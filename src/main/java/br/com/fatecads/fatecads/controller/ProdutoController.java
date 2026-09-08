package br.com.fatecads.fatecads.controller;

import java.util.List;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.fatecads.fatecads.entity.Produto;
import br.com.fatecads.fatecads.service.ProdutoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;




@Controller
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    // Método para listar todos os produtos
    @GetMapping("/listar")
    public String listar(Model model) {
        List<Produto> produtos = produtoService.findAll();
        model.addAttribute("produtos", produtos);
        return "produto/listarProdutos";
    }

    // Método para abrir o formulário de criação de produtos
    @GetMapping("/criar")
    public String criarForm(Model model) {
        model.addAttribute("produto", new Produto());
        return "produto/formularioProduto";
    }
    
    // Método para salvar o produto no banco de dados
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Produto produto,
                         @RequestParam(value = "imagem", required = false) MultipartFile imagem) {
        if (imagem != null && !imagem.isEmpty()) {
            String tipoImagem = imagem.getContentType();
            if (tipoImagem == null || !tipoImagem.startsWith("image/")) {
                throw new IllegalArgumentException("O arquivo enviado precisa ser uma imagem");
            }
            try {
                produto.setImagemProduto(imagem.getBytes());
                produto.setTipoImagemOriginal(tipoImagem);
            } catch (IOException exception) {
                throw new IllegalStateException("Não foi possível ler a imagem enviada", exception);
            }
        }
        produtoService.save(produto);
        return "redirect:/produtos/listar";
    }
    
    // Método para abrir o formulário de edição de produtos
    @GetMapping("/editar/{id}")
    public String editarForm(@PathVariable Integer id, Model model) {
        Produto produto = produtoService.findById(id);
        model.addAttribute("produto", produto);
        return "produto/formularioProduto";
    }

    @GetMapping("/{id}/imagem")
    @ResponseBody
    public ResponseEntity<byte[]> imagem(@PathVariable Integer id) {
        Produto produto = produtoService.findById(id);
        if (produto == null || produto.getImagemProduto() == null || produto.getImagemProduto().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipo = MediaType.parseMediaType(
                produto.getTipoImagemOriginal() != null ? produto.getTipoImagemOriginal() : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, tipo.toString())
                .body(produto.getImagemProduto());
    }
    
    // Método para excluir um produto pelo ID
    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Integer id) {
        produtoService.deleteById(id);
        return "redirect:/produtos/listar";
    }
    
    
}
