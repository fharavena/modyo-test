package modyo.com.pokedex.models;

import lombok.Data;
import java.util.List;

@Data
public class PokemonOutDTO {
    private Integer count;
    private String next;
    private String previous;
    private List<PokemonResultOutDTO> results;
}
