import com.google.inject.Singleton;
import org.rspeer.game.script.model.ConfigModel;
import org.rspeer.game.script.model.ui.schema.text.TextFieldComponent;
import org.rspeer.game.script.model.ui.schema.text.TextInputType;

@Singleton
public class Config extends ConfigModel {

  @TextFieldComponent(name = "Leather name", key = "leather_name", placeholder = "Name of leather (e.g. Blue dragon leather)")
  private String leatherName;

  @TextFieldComponent(name = "Leather per product", key = "per_product", inputType = TextInputType.NUMERIC, placeholder = "Leather per product (e.g. d'hide body needs 3)")
  private int leatherPerProduct;

  @TextFieldComponent(name = "Product name", key = "product_name", placeholder = "Name of product (e.g. Blue d'hide body)")
  private String productName;

  public String getLeatherName() {
    return leatherName;
  }

  public int getLeatherPerProduct() {
    return leatherPerProduct;
  }

  public String getProductName() {
    return productName;
  }
}
