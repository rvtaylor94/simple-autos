package com.galvanize.simpleautos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AutosController.class)
public class AutosControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AutosService autosService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String path = "/api/autos";
    private final String updateAutoJson = "{\"color\": \"RED\",\"owner\": \"Bob\"}";
    private Automobile automobile;

    @BeforeEach
    void setUp() {
        automobile = new Automobile(1967, "Ford", "Mustang", "AABBCC");

    }

    @Test
    void getRequestWithNoVinOrParamsReturnsAllAutos() throws Exception {
        List<Automobile> automobiles = populateAutos();
        when(autosService.getAutos()).thenReturn(new AutosList(automobiles));
        mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    @Test
    void getRequestNoParamsReturnsNothing() throws Exception {
        when(autosService.getAutos()).thenReturn(new AutosList());
        mockMvc.perform(get(path))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRequestWithSearchParamsReturnsList() throws Exception {
        List<Automobile> automobiles = populateAutos();
        when(autosService.getAutos(anyString(), anyString())).thenReturn(new AutosList(automobiles));
        mockMvc.perform(get(path)
                .param("color", "RED")
                .param("make", "Ford"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    @Test
    void getRequestWithColorRedReturnsRedAutos() throws Exception {
        List<Automobile> automobiles = populateAutos();
        for (int i = 0; i < 5; i++) {
            automobiles.get(i).setColor("RED");
        }
        when(autosService.getAutos("RED", null)).thenReturn(new AutosList(automobiles));
        mockMvc.perform(get(path)
                .param("color", "RED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    @Test
    void getRequestSearchByColorNotFoundReturnsNoContent() throws Exception {
        when(autosService.getAutos("RED", null)).thenReturn(new AutosList());
        mockMvc.perform(get(path)
                .param("color", "RED"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRequestSearchByMakeNotFoundReturnsNoContent() throws Exception {
        when(autosService.getAutos(null, "Ford")).thenReturn(new AutosList());
        mockMvc.perform(get(path)
                        .param("make", "Ford"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getRequestWithMakeFordReturnsFordAutos() throws Exception {
        // populate automobiles list with 5 different autos
        List<Automobile> automobiles = populateAutos();
        // set all 5 autos to have make "Ford"
        for (int i = 0; i < 5; i++) {
            automobiles.get(i).setMake("Ford");
        }
        // when the service layer is called to get all autos with NO color and make "Ford", then return
        // the list of automobiles
        when(autosService.getAutos(null, "Ford")).thenReturn(new AutosList(automobiles));
        // perform the GET request (path is "/api/autos"
        mockMvc.perform(get(path)
                // set one param of "make" to "Ford", making path "api/autos?make=Ford
                .param("make", "Ford"))
                .andExpect(status().isOk())
                // expects size to be 5 (we're returning the full list of autos)
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    @Test
    void getRequestWithColorGreenAndMakeFordReturnsGreenFords() throws Exception {
        List<Automobile> automobiles = populateAutos();
        for (Automobile auto : automobiles) {
            auto.setColor("GREEN");
            auto.setMake("Ford");
        }
        when(autosService.getAutos("GREEN", "Ford")).thenReturn(new AutosList(automobiles));
        mockMvc.perform(get(path)
                        .param("color", "GREEN")
                        .param("make", "Ford"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.automobiles", hasSize(5)));
    }

    @Test
    void postRequestReturnsAuto() throws Exception {
        when(autosService.addAuto(any(Automobile.class))).thenReturn(automobile);
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(automobile)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Ford"));
    }

    @Test
    void postRequestBadRequestReturns400() throws Exception {
        String json = "{\"year\":1967,\"make\":\"Ford\",\"model\":\"Mustang\",\"color\":null,\"owner\":null,\"vin\":\"AABBCC\"}";
        when(autosService.addAuto(any(Automobile.class))).thenThrow(InvalidAutoException.class);
        mockMvc.perform(post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestWithVinReturnsAuto() throws Exception {
        when(autosService.getAuto(anyString())).thenReturn(automobile);
        mockMvc.perform(get(path + "/" + automobile.getVin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value(automobile.getVin()));
    }

    @Test
    void getRequestWithVinWhenNoAutosExistReturns204() throws Exception {
        when(autosService.getAuto(anyString())).thenReturn(new Automobile());
        mockMvc.perform(get(path + "/" + automobile.getVin()))
                .andExpect(status().isNoContent());
    }

    @Test
    void patchRequestWithObjectReturnsAuto() throws Exception {
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenReturn(automobile);
        mockMvc.perform(patch(path + "/" + automobile.getVin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAutoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("RED"))
                .andExpect(jsonPath("$.owner").value("Bob"));
    }

    @Test
    void patchRequestReturnsNoContentIfNotFound() throws Exception {
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenReturn(new Automobile());
        mockMvc.perform(patch(path + "/" + automobile.getVin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateAutoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void patchRequestReturns400ForBadRequest() throws Exception {
        when(autosService.updateAuto(anyString(), anyString(), anyString())).thenThrow(InvalidAutoException.class);
        mockMvc.perform(patch(path + "/" + automobile.getVin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAutoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRequestWithVinReturns202() throws Exception {
        mockMvc.perform(delete(path + "/" + automobile.getVin()))
                .andExpect(status().isAccepted());
        verify(autosService).deleteAuto(anyString());
    }

    @Test
    void deleteRequestReturnsNoContentIfNotFound() throws Exception {
        doThrow(new AutoNotFoundException()).when(autosService).deleteAuto(anyString());
        mockMvc.perform(delete(path + "/" + automobile.getVin()))
                .andExpect(status().isNoContent());
    }

    private List<Automobile> populateAutos() {
        List<Automobile> automobiles = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            automobiles.add(new Automobile(1967 + i, "Ford", "Mustang", "7F03Z01025" + i));
        }
        return automobiles;
    }

}
