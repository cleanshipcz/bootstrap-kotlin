package cz.cleanship.remento.repository

import cz.cleanship.remento.domain.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubjectRepository : JpaRepository<Subject, Long>

