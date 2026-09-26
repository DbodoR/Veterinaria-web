package com.dbodor.veterinariaweb.controller;

import com.dbodor.veterinariaweb.dto.ProductoForm;
import com.dbodor.veterinariaweb.model.Producto;
import com.dbodor.veterinariaweb.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HU-22 Catalogo de medicamentos.
 * Maestro de productos del panel de administracion.
 */
@Controller
@RequestMapping("/admin/productos")
public class AdminProductoController {

    private static final String VISTA_LISTA = "admin/productos/lista";
    private static final String VISTA_FORMULARIO = "admin/productos/formulario";

    private final ProductoService productoService;

    public AdminProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String buscar, Model model) {
        model.addAttribute("productos", productoService.listar(buscar));
        model.addAttribute("buscar", buscar == null ? "" : buscar);
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("productoForm", new ProductoForm());
        return VISTA_FORMULARIO;
    }

    /** CA1 y CA2: alta con validacion de precio y stock. */
    @PostMapping
    public String guardar(@Valid @ModelAttribute ProductoForm productoForm,
                          BindingResult errores,
                          RedirectAttributes flash) {

        if (errores.hasErrors()) {
            return VISTA_FORMULARIO;
        }

        Producto producto = new Producto();
        producto.setNombre(productoForm.getNombre().trim());
        producto.setPresentacion(productoForm.getPresentacion().trim());
        producto.setPrecioUnitario(productoForm.getPrecioUnitario());
        producto.setStock(productoForm.getStock());
        producto.setEsRecetableVeterinario(Boolean.TRUE.equals(productoForm.getEsRecetableVeterinario()));

        try {
            Producto guardado = productoService.registrarProducto(producto);
            flash.addFlashAttribute("mensajeExito",
                    "Producto " + guardado.getNombre() + " registrado correctamente.");
            return "redirect:/admin/productos";
        } catch (IllegalArgumentException e) {
            errores.reject("productoInvalido", e.getMessage());
            return VISTA_FORMULARIO;
        }
    }

    /** CA3: baja logica; el producto sigue en el listado como INACTIVO. */
    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Producto producto = productoService.desactivarProducto(id);
            flash.addFlashAttribute("mensajeExito",
                    "Producto " + producto.getNombre() + " desactivado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("mensajeError", e.getMessage());
        }
        return "redirect:/admin/productos";
    }
}