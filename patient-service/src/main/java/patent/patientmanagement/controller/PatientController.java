package patent.patientmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import patent.patientmanagement.dto.PatientRequestDTO;
import patent.patientmanagement.dto.PatientResponseDTO;
import patent.patientmanagement.model.Patient;
import patent.patientmanagement.repository.PatientRepository;
import patent.patientmanagement.service.PatientService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients(){
        return ResponseEntity.ok(patientService.getPatients());
    }

    @PostMapping
    @Operation(summary = "Create Patients")
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientRequestDTO patientRequestDTO){
        return ResponseEntity.ok(patientService.createPatient(patientRequestDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Patients")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @Validated({Default.class}) @RequestBody PatientRequestDTO patientRequestDTO,
            @PathVariable UUID id){

        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id,patientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete Patients")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id){
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}

