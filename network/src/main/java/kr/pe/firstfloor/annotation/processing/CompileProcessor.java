
package kr.pe.firstfloor.annotation.processing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic.Kind;

import kr.pe.firstfloor.annotation.Constant;
import kr.pe.firstfloor.annotation.ExtendableSingletonClass;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("*")
public class CompileProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(ExtendableSingletonClass.class);

        HashMap<TypeElement, TypeElement> extendedElements = new HashMap<>();
        for (Element annotatedElement : annotatedElements)
            for (Element rootElement : roundEnv.getRootElements())
                if (rootElement instanceof TypeElement
                && processingEnv.getTypeUtils().isAssignable(rootElement.asType(), annotatedElement.asType()))
                    extendedElements.put((TypeElement) rootElement, (TypeElement) annotatedElement);

        for (Map.Entry<TypeElement, TypeElement> entry : extendedElements.entrySet()) {
            TypeElement extendedElement = entry.getKey();
            TypeElement superclassElement = entry.getValue();

            boolean haveConstructorWithNoParameter = false;
            for (Element enclosedElement : extendedElement.getEnclosedElements()) {
                if (enclosedElement instanceof ExecutableElement
                && enclosedElement.toString().startsWith(extendedElement.getSimpleName().toString())) {
                    if (!enclosedElement.getModifiers().contains(Modifier.PROTECTED))
                        processingEnv.getMessager().printMessage(Kind.ERROR, "@ExtendableSingletonClass "
                                + (extendedElement.equals(superclassElement) ? "" : "(in superclass " + superclassElement.getQualifiedName() + ")")
                                + " annotation: expected only protected modifier at every constructor, but found modifiers "
                                + enclosedElement.getModifiers().toString() + "!", enclosedElement);
                    if (((ExecutableElement) enclosedElement).getParameters().size() == 0)
                        haveConstructorWithNoParameter = true;
                }
            }

            if (!haveConstructorWithNoParameter)
                processingEnv.getMessager().printMessage(Kind.ERROR, "@ExtendableSingletonClass "
                        + (extendedElement.equals(superclassElement) ? "" : "(in superclass " + superclassElement.getQualifiedName() + ")")
                        + " annotation: expected constructor with no parameter, but nothing found!", extendedElement);
        }

        annotatedElements = roundEnv.getElementsAnnotatedWith(Constant.class);

        HashMap<VariableElement, Integer> variableElements = new HashMap<>();
        for (Element annotatedElement : annotatedElements)
            if (annotatedElement instanceof VariableElement
            && annotatedElement.getModifiers().contains(Modifier.STATIC)
            && annotatedElement.getModifiers().contains(Modifier.FINAL)
            && annotatedElement.asType().getKind().equals(TypeKind.INT))
                variableElements.put((VariableElement) annotatedElement, (Integer) ((VariableElement) annotatedElement).getConstantValue());
            else
                processingEnv.getMessager().printMessage(Kind.ERROR, "@Constant annotation: expected static, final modifier and int type, but found modifiers "
                        +annotatedElement.getModifiers().toString()+" and type "+annotatedElement.asType().getKind().toString()+"!", annotatedElement);

        HashMap<Integer, Integer> listCount = new HashMap<>();
        for (int constant : variableElements.values())
            listCount.put(constant, (listCount.get(constant) == null) ? 1 : listCount.get(constant)+1);

        for (Map.Entry<Integer, Integer> entry : listCount.entrySet()) {
            int constant = entry.getKey();
            int count = entry.getValue();

            if (count > 1)
                for (VariableElement variableElement : variableElements.keySet())
                    if (variableElements.get(variableElement) == constant)
                        processingEnv.getMessager().printMessage(Kind.ERROR, "@Constant annotation: expected independent constants, but "+count+" duplicated constants found: "+constant, variableElement);
        }

        return true;
    }
}
