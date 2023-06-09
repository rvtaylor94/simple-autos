package com.galvanize.simpleautos;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/autos")
public class AutosController {
    AutosList automobiles = new AutosList();

    AutosService autosService;

    public AutosController(AutosService autosService) {
        this.autosService = autosService;
    }

    @GetMapping()
    public ResponseEntity<AutosList> getAllAutos(@RequestParam(required = false) String color,
                                                 @RequestParam(required = false) String make) {
        AutosList autosList;
        if (color == null && make == null) {
            autosList = autosService.getAutos();
        } else {
            autosList = autosService.getAutos(color, make);
        }
        return autosList.isEmpty() ? ResponseEntity.noContent().build() :
                ResponseEntity.ok(autosList);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Automobile addAuto(@RequestBody Automobile auto) {
        if(auto.getYear() == 0 || auto.getMake() == null || auto.getModel() == null || auto.getVin() == null) {
            throw new InvalidAutoException();
        }
        return autosService.addAuto(auto);
    }

    @GetMapping("/{vin}")
    public ResponseEntity<Automobile> getAutoByVin(@PathVariable String vin) {
        Automobile auto = autosService.getAuto(vin);
        return auto.getVin() == null ? ResponseEntity.noContent().build() :
                ResponseEntity.ok(auto);
    }

    @PatchMapping("/{vin}")
    public ResponseEntity<Automobile> updateAuto(@PathVariable String vin, @RequestBody UpdateAuto update) {
        Automobile automobile = autosService.updateAuto(vin, update.getColor(), update.getOwner());
        if (automobile.getVin() == null) {
            return ResponseEntity.noContent().build();
        }
        if (update.getColor() == null && update.getOwner() == null) {
            throw new InvalidAutoException();
        }
        automobile.setColor(update.getColor());
        automobile.setOwner(update.getOwner());
        return ResponseEntity.ok(automobile);
    }

    @DeleteMapping("/{vin}")
    public ResponseEntity deleteAuto(@PathVariable String vin) {
        try {
            autosService.deleteAuto(vin);
        } catch (AutoNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.accepted().build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void invalidAutoExceptionHandler(InvalidAutoException e) {}
}
