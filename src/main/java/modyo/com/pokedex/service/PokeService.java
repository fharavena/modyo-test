package modyo.com.pokedex.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import modyo.com.pokedex.models.PokemonDetailOutDTO;
import modyo.com.pokedex.models.PokemonOutDTO;

import java.net.URISyntaxException;

public interface PokeService {
    public PokemonOutDTO findAllPokemon(Integer offset, Integer limit,String baseUrl);
    public PokemonDetailOutDTO findPokemon(String name) ;
}
