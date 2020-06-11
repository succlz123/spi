package org.succlz123.spi;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class SpiProcessor extends BaseProcessor {
    private static final String TAG = SpiProcessor.class.getName();
    private static final String MODULE_NAME_KEY = "spi_name";

    private static final String PACKAGE_NAME = "org.succlz123.spi";
    private static final String SPI_MAPPER_CLASS_NAME = "SpiMapper";
    private String classNameSuffix;
    private String generateClassName;

    private Map<String, ApiClassParameter> apiClassHashMap = new HashMap<>();
    private Map<String, ImplClassParameter> implClassHashMap = new HashMap<>();

    Map<ApiClassParameter, List<ImplClassParameter>> processMap = new HashMap<>();
    ArrayList<Map.Entry<ApiClassParameter, List<ImplClassParameter>>> processList;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Map<String, String> options = processingEnv.getOptions();
        for (String key : options.keySet()) {
            if (key.equals(MODULE_NAME_KEY)) {
                classNameSuffix = options.get(key);
                break;
            }
        }
        if (classNameSuffix == null) {
            classNameSuffix = "App";
        }
        generateClassName = SPI_MAPPER_CLASS_NAME + classNameSuffix;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(SpiApi.class.getCanonicalName());
        supportTypes.add(SpiImpl.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String tagStr = " -> processing " + classNameSuffix;
        messager.printMessage(Diagnostic.Kind.NOTE, TAG + tagStr);
        long startTime = System.currentTimeMillis();
        Set<? extends Element> implElements = roundEnvironment.getElementsAnnotatedWith(SpiImpl.class);
        if (implElements != null && !implElements.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Element implElement : implElements) {
                sb.append(implElement.getSimpleName());
                sb.append("\n");
            }
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + tagStr + " - " + implElements.size() + "\n" + sb.toString());
            Set<? extends Element> apiElements = roundEnvironment.getElementsAnnotatedWith(SpiApi.class);
            apiClassHashMap.clear();
            implClassHashMap.clear();
            for (Element apiElement : apiElements) {
                if (apiElement instanceof TypeElement) {
                    String fullName = apiElement.asType().toString();
                    ApiClassParameter proxy = apiClassHashMap.get(fullName);
                    if (proxy == null) {
                        proxy = new ApiClassParameter(elementUtils, apiElement);
                        apiClassHashMap.put(fullName, proxy);
                    }
                }
            }
            for (Element implElement : implElements) {
                if (implElement instanceof TypeElement) {
                    String fullName = implElement.asType().toString();
                    ImplClassParameter proxy = implClassHashMap.get(fullName);
                    if (proxy == null) {
                        com.sun.tools.javac.util.List<Type> interfaces = ((Symbol.ClassSymbol) implElement).getInterfaces();
                        ApiClassParameter superClassPar = null;
                        for (Type anInterface : interfaces) {
                            superClassPar = apiClassHashMap.get(anInterface.toString());
                            if (superClassPar != null) {
                                break;
                            }
                        }
                        proxy = new ImplClassParameter(elementUtils, implElement, superClassPar);
                        implClassHashMap.put(fullName, proxy);
                    }
                }
            }
            Set<Map.Entry<String, ImplClassParameter>> entries = implClassHashMap.entrySet();
            for (Map.Entry<String, ImplClassParameter> entry : entries) {
                ImplClassParameter currentParameter = entry.getValue();
                if (currentParameter.superClassParameter != null) {
                    ApiClassParameter key = currentParameter.superClassParameter;
                    List<ImplClassParameter> implClassParameters = processMap.get(key);
                    if (implClassParameters == null) {
                        implClassParameters = new ArrayList<>();
                        processMap.put(currentParameter.superClassParameter, implClassParameters);
                    }
                    if (!implClassParameters.contains(currentParameter)) {
                        implClassParameters.add(currentParameter);
                    }
                }
            }

            Set<Map.Entry<ApiClassParameter, List<ImplClassParameter>>> processEntries = processMap.entrySet();
            processList = new ArrayList<>(processEntries);
            if (processList.isEmpty()) {
                return false;
            }

            try {
                JavaFile javaFile = JavaFile.builder(PACKAGE_NAME, generateManagerCode()).build();
                javaFile.writeTo(processingEnv.getFiler());
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, TAG + tagStr + " error " + e);
            }
            double cost = (System.currentTimeMillis() - startTime) / 1000d;
            messager.printMessage(Diagnostic.Kind.NOTE, TAG + tagStr + " finish cost time: " + cost + "s");
        }
        return true;
    }

    private TypeSpec generateManagerCode() {
        ClassName abstractMapper = ClassName.get(PACKAGE_NAME, "AbstractMapper");
        return TypeSpec.classBuilder(generateClassName)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("This Java file is automatically generated by " + TAG + ", PLEASE DO NOT EDIT!")
                .superclass(abstractMapper)
                // field
                .addField(generateStateMapFields())
                // init
                .addMethod(generateInitMethod())
                // obtain method
                .addMethod(generateObtainInstanceMethod())
                .addMethod(generateObtainInstanceByNameMethod())
                .addMethod(generateObtainInstanceByAllMethod())
                // check method
                .addMethod(generateCheckCycleMethod())
                .build();
    }

    private MethodSpec generateInitMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        Set<Map.Entry<ApiClassParameter, List<ImplClassParameter>>> processEntries = processMap.entrySet();
        for (Map.Entry<ApiClassParameter, List<ImplClassParameter>> processEntry : processEntries) {
            ApiClassParameter key = processEntry.getKey();
            List<ImplClassParameter> value = processEntry.getValue();

            String apiName = "apiClass_" + key.classSimpleName;
            String implListName = "implClasses_" + key.classSimpleName;

            methodBuilder.addStatement("String " + apiName + " = \"" + key.classFullName + "\"");
            methodBuilder.addStatement("$T<String> " + implListName + " = new ArrayList<>()", ArrayList.class);
            for (ImplClassParameter implClassParameter : value) {
                String implName = "implClass_" + implClassParameter.annotationDefinedName;
                methodBuilder.addStatement("String " + implName + " = \"" + implClassParameter.classFullName + "\"");
                methodBuilder.addStatement(implListName + ".add(" + implName + ")");
            }
            methodBuilder.addStatement("spiMap.put(" + apiName + ", " + implListName + ")");
        }
        return methodBuilder.build();
    }

    private MethodSpec generateObtainInstanceMethod() {
        ClassName spiObtainInfo = ClassName.get(PACKAGE_NAME, "SpiObtainInfo");
        TypeVariableName typeT = TypeVariableName.get("T");
        ParameterizedTypeName clzType = ParameterizedTypeName.get(spiObtainInfo, typeT);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("obtainInstance")
                .addParameter(String.class, "apiClassName")
                .addAnnotation(ClassName.get(Override.class))
                .addTypeVariable(typeT)
                .returns(clzType)
                .addModifiers(Modifier.PUBLIC);
        String resultName = spiObtainInfo.toString();
        methodBuilder.addStatement(resultName + "<T> result = new " + resultName + "<T>()");
        methodBuilder.addStatement("Object instance = null");
        for (int i = 0; i < processList.size(); i++) {
            ApiClassParameter key = processList.get(i).getKey();
            List<ImplClassParameter> value = processList.get(i).getValue();
            if (!value.isEmpty()) {
                String apiName = "apiClass_" + key.classSimpleName;
                String implClassName = value.get(0).classFullName;
                String implAnnotationDefinedName = value.get(0).annotationDefinedName;
                methodBuilder.addStatement("String " + apiName + " = \"" + key.classFullName + "\"");
                methodBuilder.beginControlFlow("if (apiClassName.equals(" + apiName + "))")
                        .addStatement("String implClassName = \"" + implClassName + "\"")
                        .addStatement(callCycleCheck())
                        .addStatement("instance = new " + implClassName + "()")
                        .addStatement(callCycleRemove())
                        .endControlFlow();

                methodBuilder.beginControlFlow("if (instance != null)")
                        .addStatement("result.obtain = (T)instance")
                        .addStatement("result.apiClassName = \"" + key.classFullName + "\"")
                        .addStatement("result.implClassName = \"" + implClassName + "\"")
                        .addStatement("result.implAnnotationDefinedName = \"" + implAnnotationDefinedName + "\"");
                if (i != processList.size() - 1) {
                    methodBuilder.addStatement("return result");
                }
                methodBuilder.endControlFlow();
            }
        }
        methodBuilder.addStatement("return result");
        return methodBuilder.build();
    }

    private MethodSpec generateObtainInstanceByNameMethod() {
        ClassName spiObtainInfo = ClassName.get(PACKAGE_NAME, "SpiObtainInfo");
        TypeVariableName typeT = TypeVariableName.get("T");
        ParameterizedTypeName clzType = ParameterizedTypeName.get(spiObtainInfo, typeT);
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("obtainInstanceByName")
                .addParameter(String.class, "apiClassName")
                .addParameter(String.class, "implAnnotationDefinedName")
                .addAnnotation(ClassName.get(Override.class))
                .addTypeVariable(typeT)
                .returns(clzType)
                .addModifiers(Modifier.PUBLIC);
        String resultName = spiObtainInfo.toString();
        methodBuilder.addStatement(resultName + "<T> result = new " + resultName + "<T>()");
        methodBuilder.addStatement("Object instance = null");
        for (int i = 0; i < processList.size(); i++) {
            ApiClassParameter key = processList.get(i).getKey();
            List<ImplClassParameter> value = processList.get(i).getValue();
            String apiName = "apiClass_" + key.classSimpleName;
            methodBuilder.addStatement("String " + apiName + " = \"" + key.classFullName + "\"");
            methodBuilder.beginControlFlow("if (apiClassName.equals(" + apiName + "))");
            for (int j = 0; j < value.size(); j++) {
                ImplClassParameter implClassParameter = value.get(j);
                String implClassName = implClassParameter.classFullName;
                String implAnnotationName = "implAnnotationName" + implClassParameter.annotationDefinedName;
                methodBuilder.addStatement("String " + implAnnotationName + " = \"" + implClassParameter.annotation.name() + "\"");
                methodBuilder.beginControlFlow("if (implAnnotationDefinedName.equals(" + implAnnotationName + "))")
                        .addStatement("String implClassName = \"" + implClassName + "\"")
                        .addStatement(callCycleCheck())
                        .addStatement("instance = new " + implClassParameter.classFullName + "()")
                        .addStatement(callCycleRemove())
                        .endControlFlow();

                methodBuilder.beginControlFlow("if (instance != null)")
                        .addStatement("result.obtain = (T)instance")
                        .addStatement("result.apiClassName = \"" + key.classFullName + "\"")
                        .addStatement("result.implClassName = \"" + implClassParameter.classFullName + "\"")
                        .addStatement("result.implAnnotationDefinedName = \"" + implClassParameter.annotationDefinedName + "\"");
                if (i != processList.size() - 1 || j != value.size() - 1) {
                    methodBuilder.addStatement("return result");
                }
                methodBuilder.endControlFlow();
            }
            methodBuilder.endControlFlow();
        }
        methodBuilder.addStatement("return result");
        return methodBuilder.build();
    }

    private MethodSpec generateObtainInstanceByAllMethod() {
        ClassName spiObtainInfo = ClassName.get(PACKAGE_NAME, "SpiObtainInfo");
        ClassName arrayList = ClassName.get(ArrayList.class);
        TypeVariableName typeT = TypeVariableName.get("T");
        ParameterizedTypeName clzType = ParameterizedTypeName.get(spiObtainInfo, typeT);
        ParameterizedTypeName returnType = ParameterizedTypeName.get(arrayList, clzType);

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("obtainInstanceByAll")
                .addParameter(String.class, "apiClassName")
                .addAnnotation(ClassName.get(Override.class))
                .returns(returnType)
                .addTypeVariable(typeT)
                .addModifiers(Modifier.PUBLIC);
        methodBuilder.addStatement("ArrayList<SpiObtainInfo<T>> result = new ArrayList<>()");
        methodBuilder.addStatement("Object instance = null");

        String resultName = spiObtainInfo.toString();

        for (int i = 0; i < processList.size(); i++) {
            ApiClassParameter key = processList.get(i).getKey();
            List<ImplClassParameter> value = processList.get(i).getValue();
            methodBuilder.beginControlFlow("if (apiClassName.equals(\"" + key.classFullName + "\"))");
            for (int j = 0; j < value.size(); j++) {
                ImplClassParameter implClassParameter = value.get(j);
                String obtainImplResultName = implClassParameter.classSimpleName + "_obtainInfo";
                String implClassName = implClassParameter.classFullName;
                methodBuilder.addStatement(resultName + " " + obtainImplResultName + " = new " + resultName + "()");
                if (j == 0) {
                    methodBuilder.addStatement("String implClassName = null");
                } else {
                    methodBuilder.addStatement("implClassName = \"" + implClassName + "\"");
                }
                methodBuilder.addStatement(callCycleCheck())
                        .addStatement(obtainImplResultName + ".obtain = (T)new " + implClassParameter.classFullName + "()")
                        .addStatement(callCycleRemove())
                        .addStatement(obtainImplResultName + ".apiClassName = apiClassName")
                        .addStatement(obtainImplResultName + ".implClassName = implClassName")
                        .addStatement(obtainImplResultName + ".implAnnotationDefinedName = \"" + implClassParameter.annotationDefinedName + "\"");
                methodBuilder.addStatement("result.add(" + obtainImplResultName + ")");
            }
            methodBuilder.endControlFlow();

            if (i != processList.size() - 1) {
                methodBuilder.beginControlFlow("if (!result.isEmpty())");
                methodBuilder.addStatement("return result")
                        .endControlFlow();
            }
        }
        methodBuilder.addStatement("return result");
        return methodBuilder.build();
    }

    private String callCycleCheck() {
        return "checkCycleOpt(implClassName);\n" +
                "instanceState.put(implClassName, \"Creating\")";
    }

    private String callCycleRemove() {
        return "instanceState.remove(implClassName)";
    }

    private FieldSpec generateStateMapFields() {
        ClassName mapClass = ClassName.get(HashMap.class);
        ClassName strClass = ClassName.get(String.class);
        ParameterizedTypeName type = ParameterizedTypeName.get(mapClass, strClass, strClass);
        return FieldSpec.builder(type, "instanceState", Modifier.PUBLIC).initializer("new HashMap<>()").build();
    }

    private MethodSpec generateCheckCycleMethod() {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("checkCycleOpt")
                .addModifiers(Modifier.PRIVATE)
                .returns(Void.TYPE)
                .addParameter(String.class, "implClassName");
        methodBuilder.addCode("String state = instanceState.get(implClassName);\n" +
                "if (\"Creating\".equals(state)) {\n" +
                "   java.util.Set<String> keys = instanceState.keySet();\n" +
                "   StringBuilder sb = new StringBuilder(\"Spi opt is cycle\\n\");\n" +
                "   for (String key : keys) {\n" +
                "       sb.append(\"======= \");\n" +
                "       sb.append(key);\n" +
                "       sb.append(\" =======\");\n" +
                "       sb.append(\"\\n\");\n" +
                "   }\n" +
                "   sb.append(\"please check the dependence!!!\");\n" +
                "   throw new IllegalStateException(sb.toString());" +
                "}");
        return methodBuilder.build();
    }

    //    private TypeSpec generateInstanceHolder() {
//        ClassName className = ClassName.get(PACKAGE_NAME, generateClassName);
//        return TypeSpec.classBuilder("_InstanceHolder")
//                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
//                .addField(FieldSpec.builder(className, "_sInstance",
//                        Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("new $T()", className).build())
//                .build();
//    }
//
//    private MethodSpec generateGetInstanceMethod() {
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getInstance")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(ClassName.get(PACKAGE_NAME, generateClassName));
//        methodBuilder.addCode("return _InstanceHolder._sInstance;");
//        return methodBuilder.build();
//    }

    private static class ApiClassParameter {
        Element element;

        SpiApi annotation;

        String packageName;
        String classSimpleName;
        String classFullName;

        public ApiClassParameter(Elements elementUtils, Element element) {
            this.element = element;
            this.annotation = element.getAnnotation(SpiApi.class);
            PackageElement packageElement = elementUtils.getPackageOf(this.element);
            this.packageName = packageElement.getQualifiedName().toString();
            this.classSimpleName = element.getSimpleName().toString();
            this.classFullName = element.asType().toString();
        }
    }

    private static class ImplClassParameter {
        Element element;

        ApiClassParameter superClassParameter;
        SpiImpl annotation;

        String packageName;
        String annotationDefinedName;
        String classSimpleName;
        String classFullName;

        public ImplClassParameter(Elements elementUtils, Element element, ApiClassParameter superClassParameter) {
            this.element = element;
            this.superClassParameter = superClassParameter;
            this.annotation = element.getAnnotation(SpiImpl.class);
            PackageElement packageElement = elementUtils.getPackageOf(this.element);
            this.packageName = packageElement.getQualifiedName().toString();
            this.classSimpleName = element.getSimpleName().toString();
            this.annotationDefinedName = this.annotation.name();
            this.classFullName = element.asType().toString();
        }
    }
}
