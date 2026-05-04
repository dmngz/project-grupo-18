package com.example.cybercert.controllers.rest;

import com.example.cybercert.dto.CertificationMapper;
import com.example.cybercert.models.Certification;
import com.example.cybercert.repositories.UserRepository;
import com.example.cybercert.services.CertificationService;
import com.example.cybercert.dto.CertificationDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationRestController {

    private final UserRepository userRepository;

    @Autowired
    private CertificationService certificationService;

    @Autowired
    private CertificationMapper certificationMapper;

    public CertificationRestController(CertificationService certificationService,
            CertificationMapper certificationMapper, UserRepository userRepository) {
        this.certificationService = certificationService;
        this.certificationMapper = certificationMapper;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<CertificationDTO>> getAllCertifications() {
        List<Certification> certifications = certificationService.findAll();
        return ResponseEntity.ok(certificationMapper.toDTOs(certifications));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CertificationDTO> getCertificationById(@PathVariable Long id) {
        return certificationService.findById(id)
                .map(certificationMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CertificationDTO> createCertification(@RequestBody CertificationDTO certificationDTO) {

        Certification certification = certificationMapper.toDomain(certificationDTO);
        certification = certificationService.createCertification(certification);
        certificationDTO = certificationMapper.toDTO(certification);

        URI location = fromCurrentRequest().path("/{id}").buildAndExpand(certificationDTO.id()).toUri();

        return ResponseEntity.created(location).body(certificationDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificationDTO> updateCertification(@PathVariable Long id,
            @RequestBody CertificationDTO certificationDTO) {

        Optional<Certification> existingCertification = certificationService.findById(id);
        if (!existingCertification.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Certification certification = certificationMapper.toDomain(certificationDTO);
        certification.setId(id);
        certification = certificationService.updateCertification(id, certification);
        certificationDTO = certificationMapper.toDTO(certification);

        return ResponseEntity.ok(certificationDTO);
    }

}




