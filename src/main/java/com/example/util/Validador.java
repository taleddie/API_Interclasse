package com.example.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    public static boolean textoPreenchido(String texto) {
        return texto != null && !texto.isBlank();
    }

    public static boolean idValido(Integer id) {
        return id != null && id > 0;
    }

    public static boolean emailValido(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean dataValida(String data) {
        if (data == null) {
            return false;
        }
        try {
            LocalDate.parse(data);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean horaValida(String hora) {
        if (hora == null) {
            return false;
        }
        try {
            LocalTime.parse(hora);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}