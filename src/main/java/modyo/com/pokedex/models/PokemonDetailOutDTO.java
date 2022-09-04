package modyo.com.pokedex.models;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class PokemonDetailOutDTO extends PokemonResultOutDTO{
    private Set<String> description;
    private List<String> evolutions;
}
