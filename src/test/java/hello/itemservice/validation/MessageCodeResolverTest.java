package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

public class MessageCodeResolverTest {
    MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

    @Test
    void setMessageCodesResolverObject(){
        String[] messageCodes = messageCodesResolver.resolveMessageCodes("required", "item");
        for (String string : messageCodes) {
            System.out.println("string = " + string);
        }

        Assertions.assertThat(messageCodes).containsExactly("required.item", "required");
    }

    @Test
    void setMessageCodesResolverField(){
        String[] messageCodes = messageCodesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        for (String string : messageCodes) {
            System.out.println("string = " + string);
        }

        Assertions.assertThat(messageCodes).containsExactly("required.item.itemName"
                                                            , "required.itemName"
                                                            , "required.java.lang.String"
                                                            , "required");
    }
}
