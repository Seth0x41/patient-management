package patent.patientmanagement.mapper;

import patent.patientmanagement.dto.PatientRequestDTO;
import patent.patientmanagement.dto.PatientResponseDTO;
import patent.patientmanagement.model.Patient;

import java.time.LocalDate;

public class PatientMapper {

    public static PatientResponseDTO toDTO(Patient patient){
        PatientResponseDTO patientDTO = new PatientResponseDTO();
        patientDTO.setAddress(patient.getAddress());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setId(patient.getId().toString());
        patientDTO.setName(patient.getName());
        patientDTO.setDateOfBirth(patient.getDateOfBirth().toString());
        return patientDTO;
    }

    public static Patient toPatient(PatientRequestDTO patientRequestDTO){
      Patient patient = new Patient();
      patient.setName(patientRequestDTO.getName());
      patient.setAddress(patientRequestDTO.getAddress());
      patient.setEmail(patientRequestDTO.getEmail());
      patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
      patient.setCreated_At(LocalDate.now());
      return patient;
    }
}
