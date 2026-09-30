module org.example.practicacrudproducto {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens org.example.practicacrudproducto to javafx.fxml, javafx.base;
    exports org.example.practicacrudproducto;
}