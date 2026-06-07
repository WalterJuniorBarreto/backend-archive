#!/bin/bash

echo "🚀 Iniciando entorno de pruebas Nivel Enterprise..."
echo "🐳 Forzando API de Docker a la versión 1.41..."

# Exportamos las variables de entorno CRÍTICAS para Fedora
export DOCKER_API_VERSION=1.41
export DOCKER_HOST=unix:///var/run/docker.sock
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock

# Ejecutamos Maven forzando a que lea estas variables
./mvnw clean test
