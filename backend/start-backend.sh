#!/bin/bash
echo "Iniciando todos los microservicios de PerfumerIA..."

# Iniciar cada servicio en segundo plano usando subshells para no perder la ruta
(cd auth-service && /opt/homebrew/bin/mvn spring-boot:run) &
AUTH_PID=$!

(cd microservice-1 && /opt/homebrew/bin/mvn spring-boot:run) &
MS1_PID=$!

(cd microservice-2 && /opt/homebrew/bin/mvn spring-boot:run) &
MS2_PID=$!

(cd profile-service && /opt/homebrew/bin/mvn spring-boot:run) &
PROFILE_PID=$!

(cd admin-service && /opt/homebrew/bin/mvn spring-boot:run) &
ADMIN_PID=$!

(cd bff && /opt/homebrew/bin/mvn spring-boot:run) &
BFF_PID=$!

echo "Todos los servicios están iniciando. Para detenerlos, presiona Ctrl+C"

# Esperar a que el usuario presione Ctrl+C para matar los procesos
trap "echo 'Deteniendo servicios...'; kill $AUTH_PID $MS1_PID $MS2_PID $PROFILE_PID $ADMIN_PID $BFF_PID 2>/dev/null; exit" INT

wait
