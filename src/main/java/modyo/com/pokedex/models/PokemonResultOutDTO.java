package modyo.com.pokedex.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PokemonResultOutDTO {
    private Integer id;
    private String name;
    private Integer weight;
    private String picture;
    private List<String> types;
    private List<String> abilities;
}
