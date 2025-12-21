package com.example.cgroove.dto;

public record ApiResponse<T>(String message, T data) {
}