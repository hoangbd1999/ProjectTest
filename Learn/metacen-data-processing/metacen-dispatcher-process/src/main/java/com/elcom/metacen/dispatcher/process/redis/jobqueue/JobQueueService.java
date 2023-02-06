package com.elcom.metacen.dispatcher.process.redis.jobqueue;

import com.elcom.metacen.dispatcher.process.constant.Constant;
import com.elcom.metacen.dispatcher.process.model.kafka.consumer.satelliteimage.SatelliteImageMessageFull;
import com.elcom.metacen.dispatcher.process.model.kafka.consumer.vsatmedia.VsatMediaMessageFull;
import com.elcom.metacen.dto.redis.Job;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobQueueService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobQueueService.class);

    @Autowired
    private RedisTemplate redisTemplate;
    
    private Map<String, JobQueue> jobQueueMap;

    private static final String JOB_KEY_TEMPLATE = "jobs#%s";
    
    // private static final Long JOB_KEY_EXPIRATION_PERIOD = 6L;
    private static Long JOB_KEY_EXPIRATION_PERIOD;

    @PostConstruct
    public void init() {
        jobQueueMap = new HashMap<>();
    }

    public boolean hasJobQueue(String queueName) {
        return jobQueueMap.containsKey(queueName);
    }

    public void registerJobQueue(String queueName) {
        
        if( Constant.REDIS_JOB_QUEUE_VSAT_MEDIA.equals(queueName) )
            // JOB_KEY_EXPIRATION_PERIOD = 70L; // 70 seconds
            JOB_KEY_EXPIRATION_PERIOD = 11L; // 11 minutes
        else if( Constant.REDIS_JOB_QUEUE_SATELLITE_IMAGE.equals(queueName) )
            // JOB_KEY_EXPIRATION_PERIOD = 610L; // 610 seconds
            JOB_KEY_EXPIRATION_PERIOD = 6L; // 6 minutes
        else {
            LOGGER.error("Init queue err, cause by invalid config queue type!");
            return;
        }
        
        if (jobQueueMap.containsKey(queueName)) {
            LOGGER.warn("Queue is already registered");
            return;
            //throw new IllegalArgumentException("Queue is already registered");
        }

        JobQueue queue = new JobQueue(queueName);

        jobQueueMap.put(queueName, queue);

        LOGGER.info("Registered queue: {}", queueName);
    }

    public Job insertToWaitingQueue(String queueName, Job job) {
        if (!jobQueueMap.containsKey(queueName)) {
            LOGGER.warn("Queue: {} does not exist", queueName);
            return null;
        }
        try {
            JobQueue jobQueue = jobQueueMap.get(queueName);

            // get unique Id for this job
            UUID uuid = UUID.randomUUID();
            job.setJobId(uuid);

            // construct timestamp data for this job and store it on Redis
            JobMetaData metaData = new JobMetaData(LocalDateTime.now());
            setMetaDataForJob(job, metaData);
            redisTemplate.opsForList().leftPush(jobQueue.getWaitingQueueName(), job);
            return job;
        } catch (Exception e) {
            LOGGER.error("ex: ", e);
        }
        return null;
    }

    public SatelliteImageMessageFull getJobSatelliteImageRawToProcess(String queueName) {
        JobQueue jobQueue = jobQueueMap.get(queueName);

        SatelliteImageMessageFull job = (SatelliteImageMessageFull) redisTemplate.opsForList().rightPopAndLeftPush(jobQueue.getWaitingQueueName(), jobQueue.getProcessingQueueName());

        if (job != null) {
            JobMetaData metaData = getMetaDataForJob(job);
            if (metaData == null) {
                LOGGER.warn("No meta data found for job: {}, queueName: {}", job.getJobId().toString(), queueName);
            } else {
                metaData.setExecutedAt(LocalDateTime.now());
                metaData.setExecutionCount(metaData.getExecutionCount() + 1);
                setMetaDataForJob(job, metaData);
            }
        }
        return job;
    }
    
    public VsatMediaMessageFull getJobVsatMediaRawToProcess(String queueName) {
        JobQueue jobQueue = jobQueueMap.get(queueName);

        VsatMediaMessageFull job = (VsatMediaMessageFull) redisTemplate.opsForList().rightPopAndLeftPush(jobQueue.getWaitingQueueName(), jobQueue.getProcessingQueueName());

        if (job != null) {
            JobMetaData metaData = getMetaDataForJob(job);
            if (metaData == null) {
                LOGGER.warn("No meta data found for job: {}, queueName: {}", job.getJobId().toString(), queueName);
            } else {
                metaData.setExecutedAt(LocalDateTime.now());
                metaData.setExecutionCount(metaData.getExecutionCount() + 1);
                setMetaDataForJob(job, metaData);
            }
        }
        return job;
    }

    public void clearProcessedJob(String queueName, Job job) {
        JobQueue jobQueue = jobQueueMap.get(queueName);

        JobMetaData metaData = getMetaDataForJob(job);
        if ( metaData == null )
            LOGGER.warn("No meta data found for job: {}, queueName: {}", job.getJobId().toString(), queueName);
        else {
//            metaData.setFinishedAt(LocalDateTime.now());
//            setMetaDataForJob(job, metaData);
            
            //TODO: xóa các job key đi
            LOGGER.info("Clear job item status: {}", redisTemplate.delete(String.format(JOB_KEY_TEMPLATE, job.getJobId().toString())));
        }
        redisTemplate.opsForList().remove(jobQueue.getProcessingQueueName(), 1, job);
    }
    
    public void clearProcessedJob2(String queueName, Job job) {
        
        JobQueue jobQueue = jobQueueMap.get(queueName);
        
        Long jobClearReturn = redisTemplate.opsForList().remove(jobQueue.getProcessingQueueName(), 1, job);
        if( jobClearReturn == null || jobClearReturn.equals(0L) )
            LOGGER.error("Error when clear queueLst");
        
        if( !redisTemplate.delete(String.format(JOB_KEY_TEMPLATE, job.getJobId().toString())) )
            LOGGER.error("Error when clear queueItem");
    }

    protected String getJobKey(UUID jobId) {
        return String.format(JOB_KEY_TEMPLATE, jobId.toString());
    }

    private void setMetaDataForJob(Job job, JobMetaData metaData) {
        String keyName = getJobKey(job.getJobId());
        redisTemplate.opsForValue().set(keyName, metaData, JOB_KEY_EXPIRATION_PERIOD, TimeUnit.MINUTES);
    }

    private JobMetaData getMetaDataForJob(Job job) {
        try {
            return (JobMetaData) redisTemplate.opsForValue().get(getJobKey(job.getJobId()));
        } catch (Exception ex) {
            LOGGER.error("ex: ", ex);
        }
        return null;
    }
}
