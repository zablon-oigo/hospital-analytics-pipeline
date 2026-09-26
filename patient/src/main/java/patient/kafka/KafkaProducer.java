package patient.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import patient.events.PatientEvent;
import patient.model.Patient;

@Service
public class KafkaProducer {

  private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

  private final KafkaTemplate<String, byte[]> kafkaTemplate;

  public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendEvent(Patient patient) {

    String patientId = patient.getId().toString();

    PatientEvent event = PatientEvent.newBuilder()
        .setPatientId(patientId)
        .setName(patient.getName())
        .setEmail(patient.getEmail())
        .setEventType("PATIENT_CREATED")
        .build();

    try {
      kafkaTemplate.send(
          "patient",
          patientId,
          event.toByteArray()
      );

      log.info("PatientCreated event sent for patientId={}", patientId);

    } catch (Exception e) {
      log.error(
          "Error sending PatientCreated event for patientId={}",
          patientId,
          e
      );
    }
  }
}