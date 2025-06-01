package com.zkm.forum.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zkm.forum.model.entity.Fitness;
import com.zkm.forum.service.FitnessService;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

public class FitnessTuominJob {
    @Resource
    private FitnessService fitnessService;
    //每天上午六点，下午一点，晚上9点，晚上12点执行
    @Scheduled(cron = "0 0 6,13,21,23 * * ?")
    public void work() {
        QueryWrapper<Fitness> fitnessQueryWrapper = new QueryWrapper<>();
        fitnessQueryWrapper.select("id");
        fitnessQueryWrapper.eq("desensitization", 0);
        List<Fitness> fitnessList = fitnessService.list(fitnessQueryWrapper);
        fitnessList.forEach(fitness -> {
            fitness.setHeight(BigDecimal.ZERO);
            fitness.setWeight(BigDecimal.ZERO);
        });
        fitnessService.updateBatchById(fitnessList);
    }
}
