package br.com.jurishome.auth.web;

import java.util.List;

public record AuthView(String username, List<String> roles) {
}
