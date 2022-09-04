package modyo.com.pokedex.service;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import modyo.com.pokedex.models.PokemonDetailOutDTO;
import modyo.com.pokedex.models.PokemonOutDTO;
import modyo.com.pokedex.models.PokemonResultOutDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

@Service()
public class PokeServiceImpl implements PokeService {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.pokedex.url}")
    private String urlPokedex;

    public PokemonOutDTO findAllPokemon(Integer offset, Integer limit,String baseUrl) {

        PokemonOutDTO response = new PokemonOutDTO();
        List<PokemonResultOutDTO> result = new ArrayList<>();

        ResponseEntity<String> listRestPokemon = getRestPokemonList(offset, limit);
        JsonNode jsonNode = getJsonFromMapper(listRestPokemon);
        Integer totalPokemon = jsonNode.get("count").intValue();

        response.setCount(totalPokemon);
        response.setPrevious(getPrevious(offset, limit,baseUrl));
        response.setNext(getNextPage(offset, limit, totalPokemon,baseUrl));

        for (JsonNode resultIn : jsonNode.get("results")) {
            result.add(makeResult(resultIn));
        }

        response.setResults(result);
        return response;
    }

    @Override
    public PokemonDetailOutDTO findPokemon(String name) {
        PokemonDetailOutDTO response = new PokemonDetailOutDTO();
        List<String> abilities = new ArrayList<>();
        List<String> types = new ArrayList<>();
        Set<String> descriptions = new LinkedHashSet<>();
        List<String> evolutions = new ArrayList<>();

        ResponseEntity<String> restPokemonDetail = getRestPokemonSimpleDetail(name);
        if (restPokemonDetail == null) {
            return null;
        }
        JsonNode jsonNode = getJsonFromMapper(restPokemonDetail);

        response.setId(jsonNode.get("id").intValue());
        response.setName(jsonNode.get("name").textValue());
        response.setWeight(jsonNode.get("weight").intValue());
        response.setPicture(jsonNode.get("sprites").get("front_default").textValue());

        for (JsonNode pokemonAbility : jsonNode.get("abilities")) {
            abilities.add(pokemonAbility.get("ability").get("name").textValue());
        }
        response.setAbilities(abilities);


        for (JsonNode pokemonType : jsonNode.get("types")) {
            types.add(pokemonType.get("type").get("name").textValue());
        }
        response.setTypes(types);

        String urlSpecie = jsonNode.get("species").get("url").textValue();
        ResponseEntity<String> restSpecies = getGeneralRequest(urlSpecie);
        jsonNode = getJsonFromMapper(restSpecies);

        for (JsonNode flavor_text_entries : jsonNode.get("flavor_text_entries")) {
            if (flavor_text_entries.get("language").get("name").textValue().equals("en")) {
                descriptions.add(flavor_text_entries.get("flavor_text").textValue());
            }
        }
        response.setDescription(descriptions);

        String urlEvolution = jsonNode.get("evolution_chain").get("url").textValue();
        ResponseEntity<String> restEvolutions = getGeneralRequest(urlEvolution);

        jsonNode = getJsonFromMapper(restEvolutions);

        evolutions.add(jsonNode.get("chain").get("species").get("name").textValue());
        JsonNode chainEvolution = jsonNode.get("chain").get("evolves_to");

        while (chainEvolution != null && chainEvolution.size() != 0) {
            for (JsonNode tempChain : chainEvolution) {
                if (tempChain.get("species").get("name") != null) {
                    evolutions.add(tempChain.get("species").get("name").textValue());
                }
            }
            chainEvolution = chainEvolution.get(0).get("evolves_to");
        }
        response.setEvolutions(evolutions);

        return response;
    }

    private PokemonResultOutDTO makeResult(JsonNode resultIn) {
        PokemonResultOutDTO tempPokemon = new PokemonResultOutDTO();
        String name = resultIn.get("name").textValue();
        List<String> abilities = new ArrayList<>();
        List<String> types = new ArrayList<>();

        ResponseEntity<String> restPokemonDetail = getRestPokemonSimpleDetail(name);
        JsonNode jsonNode = getJsonFromMapper(restPokemonDetail);

        tempPokemon.setId(jsonNode.get("id").intValue());
        tempPokemon.setName(jsonNode.get("name").textValue());
        tempPokemon.setWeight(jsonNode.get("weight").intValue());
        tempPokemon.setPicture(jsonNode.get("sprites").get("front_default").textValue());

        for (JsonNode pokemonAbility : jsonNode.get("abilities")) {
            abilities.add(pokemonAbility.get("ability").get("name").textValue());
        }

        for (JsonNode pokemonType : jsonNode.get("types")) {
            types.add(pokemonType.get("type").get("name").textValue());
        }

        tempPokemon.setAbilities(abilities);
        tempPokemon.setTypes(types);
        return tempPokemon;
    }

    private JsonNode getJsonFromMapper(ResponseEntity<String> listRestPokemon) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(listRestPokemon.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextPage(Integer offset, Integer limit, Integer totalPokemon,String baseUrl) {
        return (offset + limit >= totalPokemon) ? null : baseUrl + "all/?offset=" + (offset + limit) + "&limit" + limit;
    }

    private String getPrevious(Integer offset, Integer limit,String baseUrl) {
        int previousPage = Math.max((offset - limit), 0);
        return (offset == 0) ? null : baseUrl + "all/?offset=" + previousPage + "&limit" + limit;
    }

    @Cacheable("pokemonSimpleDetail")
    private ResponseEntity<String> getRestPokemonSimpleDetail(String name) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Application");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseJson;
        try {
            responseJson = restTemplate.exchange(urlPokedex+name, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            responseJson = null;
        }
        return responseJson;

    }

    @Cacheable("pokemonList")
    private ResponseEntity<String> getRestPokemonList(Integer offset, Integer limit) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Application");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(urlPokedex + "?offset="+offset+"&limit="+limit, HttpMethod.GET, entity, String.class);
    }

    @Cacheable("generalRequest")
    private ResponseEntity<String> getGeneralRequest(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("user-agent", "Application");
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
}
