```mermaid
classDiagram

    class Autoescola {
        -codi : String
        -nom : String
        -adreca : String
        +afegirAlumne(a : Alumne) boolean
    }

    class Alumne {
        -DNI : String
        -nom : String
        -telefon : String
        +obtenirCostTotalPractiques() double
        +crearPractica(data : DateTime,durada : int, cost : Double ) boolean
    }

    class Practica {
        -dataRealitzacio : DateTime
        -duradaMinuts : int
        -cost : double
    }

    class PracticaCotxe {
        -canviAutomatic : boolean
    }

    class PracticaMoto {
        -cilindrada : int
    }

    Practica <|-- PracticaCotxe
    Practica <|-- PracticaMoto

    Autoescola "1" o--> "1..*" Alumne : registra
    Alumne "1" *--> "0..*" Practica : realitza

    note for Practica "duradaMinuts > 0"
```