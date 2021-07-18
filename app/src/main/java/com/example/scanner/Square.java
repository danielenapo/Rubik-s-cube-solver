package com.example.scanner;

import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;


public class Square {
    private Scalar colorRgb; //colore medio all'interno del quadrato
    private Rect rect; //coordinate del quadrato da disegnare su schermo
    private Point center; //coordinate del centro del quadrato
    private int size; //grandezza del quadrato (uguale per tutti)
    private String prec="n"; //carattere in cui si salva il colore precedente (per stabilizzare la lettura

    public Square(Point center, int size) {
        colorRgb = new Scalar(255,0,0);
        this.size = size;
        this.center = center;

        rect = new Rect();
        rect.x = (int) (center.x - size / 2);
        rect.y = (int) (center.y - size / 2);
        rect.width = size;
        rect.height = size;
    }

    public Scalar getColorRgb() {
        return colorRgb;
    }

    public void setColorRgb(Scalar colorHsv) {
        this.colorRgb = colorHsv;
    }

    public Rect getRect() {
        return rect;
    }

    public Point getTopLeftPoint() {
        return new Point(rect.x, rect.y);
    }

    public Point getBottomRightPoint() {
        return new Point(rect.x + rect.width, rect.y + rect.height);
    }

    public Point getCenter() {
        return center;
    }

    public int getSize() {
        return size;
    }

    //metodo che ritorna il nome del colore contenuto nel quadrato
    public String getColor() {
        String tempString; //stringa su cui salvo il nome del colore

        //ASSEGNO UN COLORE IN BASE AL RANGE RGB (fatto un po' a caso seguendo https://www.rapidtables.com/web/color/RGB_Color.html)
        if (colorRgb.val[0] >= 100 && colorRgb.val[1] < 100 && colorRgb.val[2] < 100) {
            tempString = "R";
        } else if (colorRgb.val[0] < 100 && colorRgb.val[1] >= 100 && colorRgb.val[2] < 100) {
            tempString = "G";
        } else if (colorRgb.val[0] < 100 && colorRgb.val[1] < 100 && colorRgb.val[2] >= 50) {
            tempString = "B";
        } else if (colorRgb.val[0] >= 150 && colorRgb.val[1] >= 170 && colorRgb.val[2] < 100) {
            tempString = "Y";
        } else if (colorRgb.val[0] >= 150 && colorRgb.val[1] >= 80 && colorRgb.val[1]<=200  && colorRgb.val[2] < 100) {   //a volte da problemi (rosso, giallo e arancione si confondono in base alla luce)
            tempString = "O";
        } else if (colorRgb.val[0] > 150 && colorRgb.val[1] > 150 && colorRgb.val[2] > 150) {
            tempString = "W";
        } else {
            tempString = prec; //se non riesco a leggere il colore, probabilmente è dato dalla luce, per stabilizzare i risultati metto l'ultimo colore letto;
        }

        prec=tempString; //salvo il colore letto in prec

        return tempString;
    }

}
