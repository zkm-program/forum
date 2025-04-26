package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Fitness;
import generator.service.FitnessService;
import generator.mapper.FitnessMapper;
import org.springframework.stereotype.Service;

/**
* @author 张凯铭
* @description 针对表【fitness(健身表)】的数据库操作Service实现
* @createDate 2025-04-24 11:21:37
*/
@Service
public class FitnessServiceImpl extends ServiceImpl<FitnessMapper, Fitness>
    implements FitnessService{

}




