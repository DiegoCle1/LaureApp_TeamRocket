package it.uniba.dib.sms222312.modelli.compara;

import java.util.Comparator;

import it.uniba.dib.sms222312.modelli.Classifica;

public class ComparaPos implements Comparator<Classifica> {
        @Override
        public int compare(Classifica classifica, Classifica t1) {
            return classifica.getPos()-t1.getPos();
        }
}
