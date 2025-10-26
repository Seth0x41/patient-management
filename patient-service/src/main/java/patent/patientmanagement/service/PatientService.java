package patent.patientmanagement.service;


import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import patent.patientmanagement.dto.PatientRequestDTO;
import patent.patientmanagement.dto.PatientResponseDTO;
import patent.patientmanagement.exception.EmailAlreadyExistException;
import patent.patientmanagement.exception.PatientNotFoundException;
import patent.patientmanagement.grpc.BillingServiceGrpcClient;
import patent.patientmanagement.kafka.KafkaProducer;
import patent.patientmanagement.mapper.PatientMapper;
import patent.patientmanagement.model.Patient;
import patent.patientmanagement.repository.PatientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final KafkaProducer kafkaProducer;
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        return patients.stream()
                .map(PatientMapper::toDTO).toList();
    }


    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw  new EmailAlreadyExistException("Email address already exist!");
        }
        Patient patient = patientRepository.save(PatientMapper.toPatient(patientRequestDTO));
        billingServiceGrpcClient.createBillingAccount(
                patient.getId().toString(),patient.getName(),patient.getEmail());

        kafkaProducer.sendEvent(patient);
        return PatientMapper.toDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient patient = patientRepository.findById(id).orElseThrow(
                ()-> new PatientNotFoundException("User Not Found!"));

        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)){
            throw new EmailAlreadyExistException("Email address already exist!");
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
