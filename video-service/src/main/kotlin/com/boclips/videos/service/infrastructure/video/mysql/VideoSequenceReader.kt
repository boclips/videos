package com.boclips.videos.service.infrastructure.video.mysql

import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.springframework.beans.factory.annotation.Autowired
import javax.persistence.EntityManagerFactory
import kotlin.streams.asSequence

typealias VideoEntitySequenceConsumer = (Sequence<VideoEntity>) -> Unit

class VideoSequenceReader(private val fetchSize: Int) {

    @Autowired
    lateinit var entityManagerFactory: EntityManagerFactory

    fun readOnly(consumer: VideoEntitySequenceConsumer) {
        val session = createStatelessSession()
        val tx = session.beginTransaction()

        val query = session.createNativeQuery("SELECT * FROM metadata_orig", VideoEntity::class.java)
        query.isReadOnly = true
        query.fetchSize = fetchSize
        query.resultStream.use { stream ->
            consumer(stream.asSequence())
        }

        tx.commit()
        session.close()
    }

    private fun createStatelessSession(): StatelessSession {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactory::class.java)
        return sessionFactory.openStatelessSession()
    }
}