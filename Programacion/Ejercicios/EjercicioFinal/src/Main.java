void main() {
    System.out.println("=================================");
    System.out.println("  CASOS DE PRUEBA ESCUELA SURF");
    System.out.println("=================================");

    System.out.println();
    System.out.println("=== CREACIÓN DE ALUMNOS ===");

    Alumne alumne1 = new Alumne("Marc", 18);
    Alumne alumne2 = new Alumne("Laia", 21);
    Alumne alumne3 = new Alumne("Joan", 15);
    Alumne alumne4 = new Alumne("Marta", 30);

    System.out.println("Alumno 1 creado correctamente.");
    System.out.println("Alumno 2 creado correctamente.");
    System.out.println("Alumno 3 creado correctamente.");
    System.out.println("Alumno 4 creado correctamente.");

    System.out.println();
    System.out.println("=== CREACIÓN DE PLAYAS ===");

    Platja platja1 = new Platja("Platja del Postiguet", 25);
    Platja platja2 = new Platja("Platja de Sant Joan", 40);
    Platja platja3 = new Platja("Platja de la Malva-rosa", 60);

    System.out.println("Playa 1 creada correctamente.");
    System.out.println("Playa 2 creada correctamente.");
    System.out.println("Playa 3 creada correctamente.");

    System.out.println();
    System.out.println("=== CREACIÓN DE MONITORES ===");

    Monitor monitor1 = new Monitor("12345678A", "Pau", "Surf iniciació");
    Monitor monitor2 = new Monitor("87654321B", "Anna", "Surf intermedi");
    Monitor monitor3 = new Monitor("11223344C", "Carles", "Surf avançat");

    System.out.println("Monitor 1 creado correctamente.");
    System.out.println("Monitor 2 creado correctamente.");
    System.out.println("Monitor 3 creado correctamente.");

    System.out.println();
    System.out.println("=== CASOS DE PRUEBA DE CLASSES DE SURF ===");

    ClasseSurf classeIniciacio1 = new ClasseSurf(
            "ACT001",
            "Classe iniciació matí",
            60,
            "10:00",
            20.0,
            NivellSurf.INICIACIO,
            platja1,
            monitor1
    );

    ClasseSurf classeIniciacio2 = new ClasseSurf(
            "ACT002",
            "Classe iniciació vesprada",
            90,
            "17:00",
            30.0,
            NivellSurf.INICIACIO,
            platja2,
            monitor1
    );

    ClasseSurf classeIntermedi1 = new ClasseSurf(
            "ACT003",
            "Classe intermèdia matí",
            60,
            "11:30",
            20.0,
            NivellSurf.INTERMEDI,
            platja2,
            monitor2
    );

    ClasseSurf classeIntermedi2 = new ClasseSurf(
            "ACT004",
            "Classe intermèdia vesprada",
            120,
            "18:30",
            35.0,
            NivellSurf.INTERMEDI,
            platja3,
            monitor2
    );

    ClasseSurf classeAvancat1 = new ClasseSurf(
            "ACT005",
            "Classe avançada matí",
            90,
            "09:00",
            25.0,
            NivellSurf.AVANCAT,
            platja3,
            monitor3
    );

    ClasseSurf classeAvancat2 = new ClasseSurf(
            "ACT006",
            "Classe avançada intensiva",
            180,
            "16:00",
            50.0,
            NivellSurf.AVANCAT,
            platja1,
            monitor3
    );

    System.out.println("Todas las clases de surf se han creado correctamente.");

    System.out.println();
    System.out.println("=================================");
    System.out.println(" CASO 1: NIVELL INICIACIO");
    System.out.println("=================================");

    classeIniciacio1.mostrarInformacio();
    System.out.println("Precio base esperado: 20.0€");
    System.out.println("Precio final obtenido: " + classeIniciacio1.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: true");
    System.out.println("Resultado obtenido permetBaixa: " + classeIniciacio1.permetBaixa());

    System.out.println();

    classeIniciacio2.mostrarInformacio();
    System.out.println("Precio base esperado: 30.0€");
    System.out.println("Precio final obtenido: " + classeIniciacio2.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: true");
    System.out.println("Resultado obtenido permetBaixa: " + classeIniciacio2.permetBaixa());

    System.out.println();
    System.out.println("=================================");
    System.out.println(" CASO 2: NIVELL INTERMEDI");
    System.out.println("=================================");

    classeIntermedi1.mostrarInformacio();
    System.out.println("Precio base esperado: 20.0€");
    System.out.println("Incremento esperado: 5.0€");
    System.out.println("Precio final esperado: 25.0€");
    System.out.println("Precio final obtenido: " + classeIntermedi1.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: true");
    System.out.println("Resultado obtenido permetBaixa: " + classeIntermedi1.permetBaixa());

    System.out.println();

    classeIntermedi2.mostrarInformacio();
    System.out.println("Precio base esperado: 35.0€");
    System.out.println("Incremento esperado: 5.0€");
    System.out.println("Precio final esperado: 40.0€");
    System.out.println("Precio final obtenido: " + classeIntermedi2.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: true");
    System.out.println("Resultado obtenido permetBaixa: " + classeIntermedi2.permetBaixa());

    System.out.println();
    System.out.println("=================================");
    System.out.println(" CASO 3: NIVELL AVANCAT");
    System.out.println("=================================");

    classeAvancat1.mostrarInformacio();
    System.out.println("Precio base esperado: 25.0€");
    System.out.println("Incremento esperado: 10.0€");
    System.out.println("Precio final esperado: 35.0€");
    System.out.println("Precio final obtenido: " + classeAvancat1.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: false");
    System.out.println("Resultado obtenido permetBaixa: " + classeAvancat1.permetBaixa());

    System.out.println();

    classeAvancat2.mostrarInformacio();
    System.out.println("Precio base esperado: 50.0€");
    System.out.println("Incremento esperado: 10.0€");
    System.out.println("Precio final esperado: 60.0€");
    System.out.println("Precio final obtenido: " + classeAvancat2.calcularPreuFinal() + "€");
    System.out.println("Resultado esperado permetBaixa: false");
    System.out.println("Resultado obtenido permetBaixa: " + classeAvancat2.permetBaixa());

    System.out.println();
    System.out.println("=================================");
    System.out.println(" RESUMEN DE CASOS ESPERADOS");
    System.out.println("=================================");

    System.out.println("INICIACIO:");
    System.out.println("- Precio final = precio base");
    System.out.println("- Permite baja = true");

    System.out.println();

    System.out.println("INTERMEDI:");
    System.out.println("- Precio final = precio base + 5");
    System.out.println("- Permite baja = true");

    System.out.println();

    System.out.println("AVANCAT:");
    System.out.println("- Precio final = precio base + 10");
    System.out.println("- Permite baja = false");

    System.out.println();
    System.out.println("=================================");
    System.out.println(" FIN DE TODOS LOS CASOS DE PRUEBA");
    System.out.println("=================================");
}
