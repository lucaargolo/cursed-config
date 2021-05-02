package io.github.lucaargolo.latte.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection"})
public class ConfigImpl {

    private final String aString = "string";
    private final int anInt = 1;
    private final short aShort = 2;
    private final long aLong = 3;
    private final double aDouble = 4.0;
    private final float aFloat = 5.0f;
    private final byte aByte = 6;
    private final char aChar = 'a';

    public static class ClassWithBasicConstructor {
        private final String anInnerString = "innerString";
        private final int anInnerInt = 123;
    }

    private final ClassWithBasicConstructor classWithBasicConstructor = new ClassWithBasicConstructor();

    public static class ClassWithoutBasicConstructor {
        private final String anInnerString;
        private final int anInnerInt;

        public ClassWithoutBasicConstructor(String aString, int anInt) {
            this.anInnerString = aString;
            this.anInnerInt = anInt;
        }
    }

    private final ClassWithoutBasicConstructor classWithoutBasicConstructor = new ClassWithoutBasicConstructor("innerString", 123);

    private final String[] strings = new String[] {"Adrian", "Beatrice", "Cole", "Daniel"};
    private final int[] ints = new int[] {1, 2, 3, 4, 5};

    private final ClassWithBasicConstructor[] classWithBasicConstructors = new ClassWithBasicConstructor[] {new ClassWithBasicConstructor(), new ClassWithBasicConstructor(), new ClassWithBasicConstructor()};
    private final List<ClassWithBasicConstructor> emptyListOfClassWithBasicConstructor = new ArrayList<>();
    private final List<ClassWithBasicConstructor> fullListOfClassWithBasicConstructor = new ArrayList<>();

    private final ClassWithoutBasicConstructor[] classWithoutBasicConstructors = new ClassWithoutBasicConstructor[] {new ClassWithoutBasicConstructor("innerString1", 123), new ClassWithoutBasicConstructor("innerString2", 456), new ClassWithoutBasicConstructor("innerString3", 789)};
    private final List<ClassWithoutBasicConstructor> emptyListOfClassWithoutBasicConstructor = new ArrayList<>();
    private final List<ClassWithoutBasicConstructor> fullListOfClassWithoutBasicConstructor = new ArrayList<>();

    private final Map<String, String> stringStringMap = new HashMap<>();
    private final Map<Integer, String> integerStringMap = new HashMap<>();
    private final Map<Integer, Integer> integerIntegerMap = new HashMap<>();

    private final Map<String, ClassWithBasicConstructor> stringClassWithBasicConstructorMap = new HashMap<>();
    private final Map<String, ClassWithoutBasicConstructor> stringClassWithoutBasicConstructorMap = new HashMap<>();

    public ConfigImpl() {
        fullListOfClassWithBasicConstructor.add(new ClassWithBasicConstructor());
        fullListOfClassWithBasicConstructor.add(new ClassWithBasicConstructor());
        fullListOfClassWithBasicConstructor.add(new ClassWithBasicConstructor());

        fullListOfClassWithoutBasicConstructor.add(new ClassWithoutBasicConstructor("innerString1", 123));
        fullListOfClassWithoutBasicConstructor.add(new ClassWithoutBasicConstructor("innerString2", 456));
        fullListOfClassWithoutBasicConstructor.add(new ClassWithoutBasicConstructor("innerString3", 789));

        stringStringMap.put("string1", "string");
        stringStringMap.put("string2", "string");
        stringStringMap.put("string3", "string");

        integerStringMap.put(1, "string");
        integerStringMap.put(2, "string");
        integerStringMap.put(3, "string");

        integerIntegerMap.put(1, 3);
        integerIntegerMap.put(2, 2);
        integerIntegerMap.put(3, 1);

        stringClassWithBasicConstructorMap.put("string1", new ClassWithBasicConstructor());
        stringClassWithBasicConstructorMap.put("string2", new ClassWithBasicConstructor());
        stringClassWithBasicConstructorMap.put("string3", new ClassWithBasicConstructor());

        stringClassWithoutBasicConstructorMap.put("string1", new ClassWithoutBasicConstructor("innerString1", 123));
        stringClassWithoutBasicConstructorMap.put("string2", new ClassWithoutBasicConstructor("innerString2", 456));
        stringClassWithoutBasicConstructorMap.put("string3", new ClassWithoutBasicConstructor("innerString3", 789));
    }

}
