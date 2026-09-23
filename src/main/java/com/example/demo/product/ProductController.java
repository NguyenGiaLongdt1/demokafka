import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/practice/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public String get(@PathVariable("id") Long id)
            throws InterruptedException {
        return service.getProduct(id);
    }
}