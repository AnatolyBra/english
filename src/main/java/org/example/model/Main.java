package org.example.model;

import org.junit.jupiter.api.Assertions;

import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<User> users = List.of(
                new User("Bob", "Adams", 30),
                new User("Ann", "Smith", 25),
                new User("Ann", "Brown", 40),
                new User("Ann", "Brown", 22)
        );

        var actual = getSortedUsers(users);
        var expected = getSortedUsersExpected(users);


        Assertions.assertEquals(expected, actual, "List not equals");
    }

    public static List<User> getSortedUsers(List<User> list) {
        return list.stream()
                .sorted((u1, u2) -> {
                    int byName = u1.userName().compareTo(u2.userName());
                    if (byName != 0) {
                        return byName;
                    }
                    int bySurname = u1.surname().compareTo(u2.surname());
                    if (bySurname != 0) {
                        return bySurname;
                    }
                    return Integer.compare(u1.age(), u2.age());
                })
                .toList();
    }

    public static List<User> getSortedUsersExpected(List<User> list) {
        return list.stream()
                .sorted(
                        Comparator.comparing(User::userName)
                                .thenComparing(User::surname)
                                .thenComparingInt(User::age)
                )
                .toList();
    }
}

record User(String userName, String surname, int age) {}