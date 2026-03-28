package com.validation.strategy;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import com.validation.annotation.Email;
import com.validation.annotation.NotEmpty;
import com.validation.annotation.NotNull;
import com.validation.annotation.NrIndeksu;
import com.validation.annotation.Size;

public class ValidationStrategyFactory {
    private static final Map<Class<? extends Annotation>, ValidationStrategy>
            strategies = new HashMap<>();
    static {
        strategies.put(NotNull.class, new NotNullStrategy());
        strategies.put(NotEmpty.class, new NotEmptyStrategy());
        strategies.put(Email.class, new EmailStrategy());
        strategies.put(NrIndeksu.class, new NrIndeksuStrategy());
        strategies.put(Size.class, new SizeStrategy());
    }
    private ValidationStrategyFactory() {
    }
    public static ValidationStrategy getStrategy(Annotation annotation) {
        return strategies.get(annotation.annotationType());
    }

}
