package org.example.practicacrudproducto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.practicacrudproducto.Modelos.Categoria;
import org.example.practicacrudproducto.Modelos.Producto;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    // Controles del formulario
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtExistencia;
    @FXML private CheckBox chkActivo;

    // Controles de Búsqueda y Filtro
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbFiltroEstado;
    @FXML private ComboBox<Categoria> cbFiltroCategoria;

    // TableView y Columnas
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, Categoria> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colExistencia;
    @FXML private TableColumn<Producto, Boolean> colActivo;

    // Colecciones
    private ObservableList<Producto> productos;
    private FilteredList<Producto> productosFiltrados;
    private Producto productoSeleccionado;
    private int idCounter = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnas();
        inicializarColecciones();
        cargarDatosFiltros();
        configurarFiltrosYBusqueda();
        configurarSeleccionTabla();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
    }

    private void inicializarColecciones() {
        productos = FXCollections.observableArrayList();
        productosFiltrados = new FilteredList<>(productos, p -> true);

        SortedList<Producto> productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tablaProductos.comparatorProperty());
        tablaProductos.setItems(productosOrdenados);
    }

    private void cargarDatosFiltros() {
        Categoria cat1 = new Categoria(1, "Electrónica");
        Categoria cat2 = new Categoria(2, "Línea Blanca");
        Categoria cat3 = new Categoria(3, "Accesorios");

        cbCategoria.getItems().addAll(cat1, cat2, cat3);

        cbFiltroCategoria.getItems().add(null);
        cbFiltroCategoria.getItems().addAll(cat1, cat2, cat3);

        cbFiltroEstado.getItems().addAll("Todos", "Activos", "Inactivos");
        cbFiltroEstado.setValue("Todos");
    }

    private void configurarSeleccionTabla() {
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productoSeleccionado = newSelection;
                txtCodigo.setText(productoSeleccionado.getCodigo());
                txtNombre.setText(productoSeleccionado.getNombre());
                cbCategoria.setValue(productoSeleccionado.getCategoria());
                txtPrecioVenta.setText(productoSeleccionado.getPrecioVenta().toString());
                txtExistencia.setText(String.valueOf(productoSeleccionado.getExistencia()));
                chkActivo.setSelected(productoSeleccionado.isActivo());
            }
        });
    }

    private void configurarFiltrosYBusqueda() {
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        productosFiltrados.setPredicate(producto -> {
            String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().toLowerCase();
            boolean coincideTexto = producto.getNombre().toLowerCase().contains(texto) ||
                    producto.getCodigo().toLowerCase().contains(texto);

            String estado = cbFiltroEstado.getValue();
            boolean coincideEstado = estado.equals("Todos") ||
                    (estado.equals("Activos") && producto.isActivo()) ||
                    (estado.equals("Inactivos") && !producto.isActivo());

            Categoria catFiltro = cbFiltroCategoria.getValue();
            boolean coincideCategoria = (catFiltro == null) || (producto.getCategoria().getId().equals(catFiltro.getId()));

            return coincideTexto && coincideEstado && coincideCategoria;
        });
    }

    @FXML
    public void guardarProducto() {
        if (!validarDatos(false)) return;

        Producto nuevoProducto = new Producto(
                idCounter++,
                txtCodigo.getText().trim(),
                txtNombre.getText().trim(),
                cbCategoria.getValue(),
                new BigDecimal(txtPrecioVenta.getText().trim()),
                Integer.parseInt(txtExistencia.getText().trim()),
                chkActivo.isSelected()
        );

        productos.add(nuevoProducto);
        limpiarFormulario();
        mostrarMensaje("Éxito", "Producto guardado.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void actualizarProducto() {
        if (productoSeleccionado == null) {
            mostrarMensaje("Atención", "Seleccione un producto.", Alert.AlertType.WARNING);
            return;
        }
        if (!validarDatos(true)) return;

        productoSeleccionado.setCodigo(txtCodigo.getText().trim());
        productoSeleccionado.setNombre(txtNombre.getText().trim());
        productoSeleccionado.setCategoria(cbCategoria.getValue());
        productoSeleccionado.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText().trim()));
        productoSeleccionado.setExistencia(Integer.parseInt(txtExistencia.getText().trim()));
        productoSeleccionado.setActivo(chkActivo.isSelected());

        tablaProductos.refresh();
        limpiarFormulario();
        mostrarMensaje("Éxito", "Producto actualizado.", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void eliminarProducto() {
        if (productoSeleccionado == null) {
            mostrarMensaje("Atención", "Seleccione un producto.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText("¿Eliminar " + productoSeleccionado.getCodigo() + "?");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            productos.remove(productoSeleccionado);
            limpiarFormulario();
        }
    }

    @FXML
    public void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        cbCategoria.setValue(null);
        txtPrecioVenta.clear();
        txtExistencia.clear();
        chkActivo.setSelected(true);
        productoSeleccionado = null;
        tablaProductos.getSelectionModel().clearSelection();
    }

    private boolean validarDatos(boolean esActualizacion) {
        StringBuilder errores = new StringBuilder();
        String codigo = txtCodigo.getText().trim();

        if (codigo.isEmpty()) errores.append("- Código obligatorio.\n");
        if (txtNombre.getText().trim().isEmpty()) errores.append("- Nombre obligatorio.\n");
        if (cbCategoria.getValue() == null) errores.append("- Seleccione categoría.\n");

        if (!codigo.isEmpty()) {
            boolean existe = productos.stream().anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigo) &&
                    (!esActualizacion || !p.getId().equals(productoSeleccionado.getId())));
            if (existe) errores.append("- Código duplicado.\n");
        }

        try {
            if (new BigDecimal(txtPrecioVenta.getText().trim()).compareTo(BigDecimal.ZERO) <= 0)
                errores.append("- Precio debe ser mayor a 0.\n");
        } catch (Exception e) { errores.append("- Precio inválido.\n"); }

        try {
            if (Integer.parseInt(txtExistencia.getText().trim()) < 0)
                errores.append("- Existencia no negativa.\n");
        } catch (Exception e) { errores.append("- Existencia inválida.\n"); }

        if (errores.length() > 0) {
            mostrarMensaje("Error", errores.toString(), Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private void mostrarMensaje(String titulo, String contenido, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(null); a.setContentText(contenido); a.showAndWait();
    }
}