package de.redcare.githubscore.web.converter;

import de.redcare.githubscore.web.dto.SortBy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SortByConverter implements Converter<String, SortBy> {
    @Override
    public SortBy convert(String source) {
        return SortBy.valueOf(source.trim().toUpperCase());
    }
}
