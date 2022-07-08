
package br.com.gwsistemas.apoio;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import br.com.gwsistemas.exportacaoEdiClaro.ExportacaoEdiClaroBO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidaBean {
    
    private ValidaBean(){
        
    }
    
    private static Validator validationFactory(){
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }
    
    public static String valid(Object  obj ) {        
        Set<ConstraintViolation<Object>> violations = validationFactory().validate(obj) ;
        StringBuilder erros = new StringBuilder();
        
        violations.stream()
                .forEach(v -> { 
                    erros.append(v.getMessage()); 
                    log.error(v.getMessage());
                });
        
        return erros.toString();
    }
    
}
