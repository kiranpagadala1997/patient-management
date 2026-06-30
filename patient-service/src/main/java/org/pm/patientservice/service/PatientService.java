package org.pm.patientservice.service;

import lombok.extern.slf4j.Slf4j;
import org.pm.patientservice.PatientMapper;
import org.pm.patientservice.PatientRepository;
import org.pm.patientservice.dto.PatientRequestDTO;
import org.pm.patientservice.dto.PatientResponseDTO;
import org.pm.patientservice.exception.EmailAlreadyExistsException;
import org.pm.patientservice.exception.PatientNotFoundException;
import org.pm.patientservice.grpc.BillingServiceGrpcClient;
import org.pm.patientservice.kafka.KafkaProducer;
import org.pm.patientservice.model.Patient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    @Autowired
    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists" + patientRequestDTO.getEmail());
        }
        Patient patient = new Patient();
        BeanUtils.copyProperties(patientRequestDTO, patient);
        patient.setDateOfBirth(
                LocalDate.parse(patientRequestDTO.getDateOfBirth())
        );

        patient.setRegisteredDate(
                LocalDate.parse(patientRequestDTO.getRegisteredDate())
        );
        patientRepository.save(patient);

        billingServiceGrpcClient.createBillingAccount(patient.getId().toString(),
                patient.getName(), patient.getEmail());

        kafkaProducer.sendEvent(patient);

        return PatientMapper.toDTO(patient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient patient = patientRepository.findById(id).orElseThrow(

                () -> new PatientNotFoundException("Patient not found with id: "+ id)
        );

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), patient.getId())) {
            throw new EmailAlreadyExistsException("A patient with this email already exists :" + patientRequestDTO.getEmail());
        }
        BeanUtils.copyProperties(patientRequestDTO, patient);
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        patientRepository.save(patient);
        return PatientMapper.toDTO(patient);
    }

    public void deletePatient(UUID id) {
        if(patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
        }
    }
}
