/* Generatore di labirinti random */
/* Versione 1.0 */
/* Creato da Tornaghi Omar */
/* otornaghi@studenti.uninsubria.it */
/* 03/10/2020 */

import java.util.ArrayList;
import java.util.Stack;

import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

class GenLab {
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Pochi argomenti.");
            System.out.println("Sintassi comando: GenLab <lunghezzaLabirinto> <altezzaLabirinto>");
            return;
        }
        int lunghezzaLab;
        int altezzaLab;
        try {
            lunghezzaLab = Integer.valueOf(args[0]);
            altezzaLab = args.length == 1 ? lunghezzaLab : Integer.valueOf(args[1]);
        } catch (Exception ex) {
            System.out.println("Errore negli argomenti.");
            System.out.println("Sintassi comando: GenLab <lunghezzaLabirinto> <altezzaLabirinto>");
            return;
        }
        genera(lunghezzaLab, altezzaLab);

    }

    /* Classe ausiliaria per salvare informazioni sullo stato di una cella */
    private static class Cella {
        public boolean muro;

        public Cella(boolean muro) {
            this.muro = muro;
        }

        @Override
        public String toString() {
            return muro == true ? "#" : ".";
        }
    }

    /* Classe utile per memorizzare le informazioni di un punto di una matrice */
    private static class Punto {
        public int i;
        public int j;

        public Punto(int i, int j) {
            this.i = i;
            this.j = j;
        }

    }

    private static void genera(int lunghezza, int altezza) {
        if (lunghezza < 2 || altezza < 2)
            return;
        Cella[][] labirinto = new Cella[altezza][lunghezza];
        /* Inizializzo il labirinto */
        for (int i = 0; i < altezza; i++) {
            for (int j = 0; j < lunghezza; j++) {
                labirinto[i][j] = i == 0 || j == 0 || i == altezza - 1 || j == lunghezza - 1 ? new Cella(true)
                        : new Cella(false);
            }
        }
        /* Genero casualmente i muri utilizzando */
        /* l'algoritmo delle divisioni successive */
        Stack<Punto> stackCamere = new Stack<Punto>();
        /* Inserisco la prima camera (labirinto inizializzato) */
        stackCamere = inserimentoCamera(stackCamere, new Punto(1, 1), new Punto(altezza - 2, lunghezza - 2));
        while (!stackCamere.isEmpty()) {
            Punto fin = stackCamere.pop(); /* Punto finale */
            Punto ini = stackCamere.pop(); /* Punto iniziale */
            /* Cerco di inserire la riga dei muri nel mezzo della sezione */
            int posRigaMuro = ini.i + (fin.i - ini.i) / 2;
            /* Faccio lo stesso per la colonna */
            int posColonnaMuro = ini.j + (fin.j - ini.j) / 2;
            /* Creo i muri e i buchi a random */
            labirinto = divisioniSuccessive(labirinto, ini, fin, posRigaMuro, posColonnaMuro);
            /* Inserisco nello stack la sottocamera inf dx */
            Punto nuovoIni = new Punto(posRigaMuro + 1, posColonnaMuro + 1);
            Punto nuovoFin = new Punto(fin.i, fin.j);
            stackCamere = inserimentoCamera(stackCamere, nuovoIni, nuovoFin);
            /* Inserisco nello stack la sottocamera inf sx */
            nuovoIni = new Punto(posRigaMuro + 1, ini.j);
            nuovoFin = new Punto(fin.i, posColonnaMuro - 1);
            stackCamere = inserimentoCamera(stackCamere, nuovoIni, nuovoFin);
            /* Inserisco nello stack la sottocamera sup dx */
            nuovoIni = new Punto(ini.i, posColonnaMuro + 1);
            nuovoFin = new Punto(posRigaMuro - 1, fin.j);
            stackCamere = inserimentoCamera(stackCamere, nuovoIni, nuovoFin);
            /* Inserisco nello stack la sottocamera sup sx */
            nuovoIni = new Punto(ini.i, ini.j);
            nuovoFin = new Punto(posRigaMuro - 1, posColonnaMuro - 1);
            stackCamere = inserimentoCamera(stackCamere, nuovoIni, nuovoFin);
        }

        /* Aggiungo l'inizio e la fine */
        labirinto[0][1].muro = false;
        labirinto[altezza - 1][lunghezza - 2].muro = false;

        /* Salvo il labirinto su file e stampo contemporaneamente */
        String strOutput = "";
        System.out.println("Output (labirinto " + lunghezza + " x " + altezza + " ):");
        boolean salta = false;
        for (int i = 0; i < altezza; i++) {
            for (int j = 0; j < lunghezza; j++) {
                strOutput += labirinto[i][j].toString();
                /* Salto la stampa se la lunghezza è > di 159 */
                if (lunghezza > 159) {
                    salta = true;
                    continue;
                }
                if ((i == 0 && !labirinto[i][j].muro) || (i == altezza - 1 && !labirinto[i][j].muro))
                    System.out.print(ANSI_BLUE_BACKGROUND + " " + ANSI_RESET);
                else {
                    if (labirinto[i][j].muro)
                        System.out.print(ANSI_BLACK_BACKGROUND + " " + ANSI_RESET);
                    else
                        System.out.print(ANSI_WHITE_BACKGROUND + " " + ANSI_RESET);
                }

            }
            strOutput += i == altezza - 1 ? "" : "\n";
            if (!salta)
                System.out.println();
        }

        /* Salvataggio effettivo */
        try {
            FileWriter fw = new FileWriter("lab_" + String.valueOf(lunghezza) + "x" + String.valueOf(altezza) + ".txt");
            fw.write(strOutput);
            fw.close();
        } catch (IOException e) {
            System.out.println("Errore nel salvataggio del file");
            e.printStackTrace();
            return;
        }
        /* Output */
        System.out.println("\"Lab_" + lunghezza + " x " + altezza + ".txt\" salvato correttamente");

    }

    private static boolean verificaDimensioni(Punto ini, Punto fin) {
        return !((fin.i - ini.i) + 1 <= 2 || (fin.j - ini.j) + 1 <= 2);
    }

    private static Stack<Punto> inserimentoCamera(Stack<Punto> s, Punto ini, Punto fin) {
        if (verificaDimensioni(ini, fin)) {
            s.push(ini);
            s.push(fin);
        }
        return s;
    }

    private static Cella[][] divisioniSuccessive(Cella[][] labirinto, Punto pIniziale, Punto pFinale, int posRigaMuro,
            int posColonnaMuro) {
        /* Creo la riga e la colonna di muri */
        for (int j = pIniziale.j; j <= pFinale.j; j++) {
            labirinto[posRigaMuro][j].muro = true;
        }
        for (int i = pIniziale.i; i <= pFinale.i; i++) {
            labirinto[i][posColonnaMuro].muro = true;
        }
        /* Controllo che non abbia bloccato nessun uscita e divido in sezioni */
        /* Controllo riga */
        ArrayList<Punto> sezioni = new ArrayList<Punto>();
        int numCancellazioni = 0;
        if (!labirinto[posRigaMuro][pIniziale.j - 1].muro) {
            labirinto[posRigaMuro][pIniziale.j].muro = false;
            numCancellazioni++;
        } else {
            /* Sezione sx */
            sezioni.add(new Punto(posRigaMuro, pIniziale.j));
            sezioni.add(new Punto(posRigaMuro, posColonnaMuro - 1));
        }
        if (!labirinto[posRigaMuro][pFinale.j + 1].muro) {
            labirinto[posRigaMuro][pFinale.j].muro = false;
            numCancellazioni++;
        } else {
            /* Sezione dx */
            sezioni.add(new Punto(posRigaMuro, posColonnaMuro + 1));
            sezioni.add(new Punto(posRigaMuro, pFinale.j));
        }
        /* Controllo colonna */
        if (!labirinto[pIniziale.i - 1][posColonnaMuro].muro) {
            labirinto[pIniziale.i][posColonnaMuro].muro = false;
            numCancellazioni++;
        } else {
            /* Sezione sup */
            sezioni.add(new Punto(pIniziale.i, posColonnaMuro));
            sezioni.add(new Punto(posRigaMuro - 1, posColonnaMuro));
        }
        if (!labirinto[pFinale.i + 1][posColonnaMuro].muro) {
            labirinto[pFinale.i][posColonnaMuro].muro = false;
            numCancellazioni++;
        } else {
            /* Sezione inf */
            sezioni.add(new Punto(posRigaMuro + 1, posColonnaMuro));
            sezioni.add(new Punto(pFinale.i, posColonnaMuro));
        }
        /* Creo dei buchi nei muri random */
        Random r = new Random();
        for (int k = numCancellazioni; k < 3; k++) {
            /* Scelgo la sezione a random */
            int numSezioni = sezioni.size() / 2;
            int posSezione = r.nextInt((numSezioni)) * 2;
            /* Scelgo il buco nel muro a random */
            Punto pInitSezioneScelta = sezioni.get(posSezione);
            Punto pFinaSezioneScelta = sezioni.get(posSezione + 1);
            Punto posBuco;
            if (pInitSezioneScelta.i == pFinaSezioneScelta.i)/* Riga */
                posBuco = new Punto(pInitSezioneScelta.i,
                        r.nextInt((pFinaSezioneScelta.j - pInitSezioneScelta.j) + 1) + (pInitSezioneScelta.j));
            else /* Colonna */
                posBuco = new Punto(
                        r.nextInt((pFinaSezioneScelta.i - pInitSezioneScelta.i) + 1) + (pInitSezioneScelta.i),
                        pInitSezioneScelta.j);
            /* Creo il buco effettivo */
            labirinto[posBuco.i][posBuco.j].muro = false;
            /* Rimuovo la sezione */
            sezioni.remove(posSezione);
            sezioni.remove(posSezione);
        }

        return labirinto;
    }

}
