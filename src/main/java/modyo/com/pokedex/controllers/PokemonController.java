package modyo.com.pokedex.controllers;

import modyo.com.pokedex.models.PokemonDetailOutDTO;
import modyo.com.pokedex.models.PokemonOutDTO;

import modyo.com.pokedex.service.PokeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/v1/pokemon")
public class PokemonController {
    @Autowired
    private PokeServiceImpl pokeServiceImpl;

    @GetMapping("/all")
    public ResponseEntity<?> getPokemonList(
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            HttpServletRequest request
    ) {
        String baseUrl = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .build()
                .toUriString();
        PokemonOutDTO data;
        Map<String, Object> response = new HashMap<>();

//        try {
            data = pokeServiceImpl.findAllPokemon(offset, limit,baseUrl+"/api/v1/pokemon/");
//        } catch (Exception e) {
//            response.put("message", "Error 500");
//            response.put("status", "error");
//            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }

        if (data == null) {
            response.put("message", "Not found: ");
            response.put("status", "error");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }

        response.put("status", "success");
        response.put("data", data);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }

    @GetMapping("/get/{name}")
    public ResponseEntity<?> getPokemonDetail2(@PathVariable String name) {
        PokemonDetailOutDTO data;
        Map<String, Object> response = new HashMap<>();

//        try {
            data = pokeServiceImpl.findPokemon(name);
//        } catch (Exception e) {
//            response.put("message", "Error 500");
//            response.put("status", "error");
//            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
//        }

        if (data == null) {
            response.put("message", "Not found: " + name);
            response.put("status", "error");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }

        response.put("status", "success");
        response.put("data", data);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    }
}
