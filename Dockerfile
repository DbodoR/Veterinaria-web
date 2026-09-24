# ============================================================
# Imagen de Veterinaria Web
#
# Construccion en dos etapas: la primera compila el proyecto con
# Maven y la segunda conserva unicamente el JAR sobre una imagen
# ligera, de modo que el contenedor final no incluye el compilador
# ni las dependencias de construccion.
# ============================================================

# ---------- Etapa 1: construccion ----------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# El wrapper de Maven se copia primero para aprovechar la cache de
# capas: mientras el pom.xml no cambie, las dependencias no se
# vuelven a descargar en cada despliegue.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# El wrapper suele venir con saltos de linea de Windows (CRLF),
# que impiden su ejecucion en Linux. Esta linea los normaliza.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

# Las pruebas ya se ejecutaron en el entorno de desarrollo; omitirlas
# aqui acorta el tiempo de despliegue.
RUN ./mvnw clean package -DskipTests -B

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage ajusta el consumo de la maquina virtual al limite
# de memoria del contenedor, necesario en planes gratuitos.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]