<template>
  <div>
    <div class="button-group">
      <van-button 
        :type="activeButton === 'yesterday' ? 'primary' : 'default'" 
        @click="selectTime('yesterday')"
      >
        昨天
      </van-button>
      <van-button 
        :type="activeButton === 'today' ? 'primary' : 'default'" 
        @click="selectTime('today')"
      >
        今天
      </van-button>
      <van-button 
        :type="activeButton === 'analyze' ? 'primary' : 'default'" 
        @click="selectTime('analyze')"
      >
        分析
      </van-button>
    </div>
    <div class="calories-section">
      <div class="calories-info">
        <div class="calories-value">{{ caloriesDiff }}g</div>
        <div class="calories-label">剩余卡路里</div>
      </div>
      <van-circle
          v-model:current-rate="currentRate"
          :rate="caloriesRate"
          :speed="100"
          :text="caloriesDiff.value < 0 ? caloriesDiff.value.toFixed(2) : '卡路里'"
          :stroke-width="60"
          layer-color="#ebedf0"
          color="red"
          size="135"
      />
    </div>
    <div class="nutrition-section">
      <div class="nutrient-item">
        <van-circle
            v-model:current-rate="currentRate"
            :rate="proteinRate"
            :speed="100"
            :text="proteinDiff.value < 0 ? proteinDiff.value.toFixed(2) : '蛋白质'"
            :stroke-width="60"
            layer-color="#ebedf0"
            color="red"
            size="75"
        />
        <span class="nutrient-value">{{ proteinDiff }}g</span>
        <span class="nutrient-label">蛋白质</span>
      </div>
      <div class="nutrient-item">
        <van-circle
            v-model:current-rate="currentRate"
            :rate="carbohydrateRate"
            :speed="100"
            :text="carbohydrateDiff.value < 0 ? carbohydrateDiff.value.toFixed(2) : '碳水化合物'"
            :stroke-width="60"
            layer-color="#ebedf0"
            color="red"
            size="75"
        />
        <span class="nutrient-value">{{ carbohydrateDiff }}g</span>
        <span class="nutrient-label">碳水化合物</span>
      </div>
      <div class="nutrient-item">
        <van-circle
            v-model:current-rate="currentRate"
            :rate="fatRate"
            :speed="100"
            :text="fatDiff.value < 0 ? fatDiff.value.toFixed(2) : '脂肪'"
            :stroke-width="60"
            layer-color="#ebedf0"
            color="red"
            size="75"
        />
        <span class="nutrient-value">{{ fatDiff }}g</span>
        <span class="nutrient-label">脂肪</span>
      </div>
    </div>
    <div class="recent-uploads" v-if="recentUploads.length > 0">
      <h3>今天上传的食物</h3>
      <van-card
          v-for="item in recentUploads"
          :key="item.id"
          :title="item.name"
          :thumb="item.image"
          @click="navigateToPhotoAnalyse(item)"
      >
        <template #desc>
          <div>
            {{ item.calories }} 卡路里
          </div>
          <div>
            {{ item.protein }} 蛋白质
          </div>
          <div>
            {{ item.carbohydrate }} 碳水化合物
          </div>
          <div>
            {{ item.fat }} 脂肪
          </div>
          <div>
           描述 {{ item.description }} 
          </div>
        </template>
        <template #footer>
          <span>{{ item.time }}</span>
          <div :class="item.status === 0 ? 'not-eaten' : 'eaten'">
            {{ item.status === 0 ? '未食用' : item.status === 1 ? '已食用' : item.status === 3 ? '是否食用？' : '' }}
          </div>
          <div v-if="item.type === 0" class="status-indicator waiting">正在等待</div>
          <div v-else-if="item.type === 1" class="status-indicator analyzing">正在分析</div>
        </template>
      </van-card>
    </div>
    <div v-else>
      <van-empty description="今天没有上传的食物" />
    </div>
    <van-popup v-model:show="showCaloriesPopup" position="bottom" :style="{ height: '60%' }">
      <div class="upload-popup">
        <img v-if="uploadedImageUrl" :src="uploadedImageUrl" alt="Uploaded Image"
             style="max-width: 100%; height: auto; margin-bottom: 16px;"/>
        <van-field
            v-model="foodName"
            label="食物名称"
            placeholder="请输入食物名称"
            style="margin-bottom: 16px;"
        />
        <van-uploader :after-read="onRead" multiple>
          <van-button icon="photo" type="primary">上传图片</van-button>
        </van-uploader>
        <van-radio-group v-model="imageType" direction="horizontal" style="margin-top: 16px;">
          <van-radio name="food">食物</van-radio>
          <van-radio name="ingredient">配料表</van-radio>
        </van-radio-group>
        <van-field
            v-model="imageDescription"
            rows="2"
            autosize
            label="图片描述"
            type="textarea"
            maxlength="50"
            placeholder="请输入图片描述"
            show-word-limit
        />
        <van-button block type="primary" style="margin-top: 16px;" @click="submitImage">提交</van-button>
      </div>
    </van-popup>
    <van-popup v-model:show="showProfilePopup" position="bottom" :style="{ height: '60%' }">
      <div class="profile-popup">
        <van-field v-model="age" type="number" label="年龄" placeholder="请输入年龄"/>
        <van-field v-model="height" type="number" label="身高（米）" placeholder="请输入身高（米）"/>
        <van-field v-model="weight" type="number" label="体重（kg）" placeholder="请输入体重（kg）"/>
        <van-radio-group v-model="fitnessGoal" direction="horizontal" style="margin-top: 16px;">
          <van-radio name="weekly">每周</van-radio>
          <van-radio name="monthly">每月</van-radio>
          <van-radio name="bimonthly">每两个月</van-radio>
        </van-radio-group>
        <van-radio-group v-model="goalType" direction="horizontal" style="margin-top: 16px;">
          <van-radio name="lose">减脂</van-radio>
          <van-radio name="gain">增肌</van-radio>
          <van-radio name="maintain">维持</van-radio>
        </van-radio-group>
        <van-field
            v-if="goalType === 'lose' || goalType === 'gain'"
            v-model="maintainGrams"
            type="number"
            label="克数"
            placeholder="请输入克数"
        />
        <van-button block type="primary" style="margin-top: 16px;" @click="submitProfile">提交</van-button>
      </div>
    </van-popup>
    <van-button class="add-button" type="primary" icon="plus" @click="showOptions = true"/>
  </div>
  <van-popup v-model:show="showOptions" position="bottom" :style="{ height: '20%' }">
    <div class="options-popup">
      <van-button block type="primary" @click="openCaloriesPopup">卡路里追踪</van-button>
      <van-button
          v-if="status !== 2"
          block
          type="primary"
          @click="handleProfileButtonClick"
          style="margin-top: 16px;"
      >
        完善自身信息
      </van-button>
    </div>
  </van-popup>
  <van-popup v-model:show="showAIAnalysisPopup" position="bottom" :style="{ height: '20%' }">
    <div class="ai-analysis-popup">
      <h3>是否根据 AI 分析自己？</h3>
      <div class="buttons">
        <van-button block type="primary" @click="confirmAIAnalysis">确定</van-button>
        <van-button block type="default" @click="cancelAIAnalysis" style="margin-top: 16px;">取消</van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Toast } from "vant";
import axios from 'axios';
import myAxios from "../plugins/myAxios";
import route from "../config/route";

const router = useRouter();
const activeButton = ref('today');
const status=ref(4);
const timeParam = ref(0);
const selectTime = (button: string) => {
  activeButton.value = button;
  switch (button) {
    case 'yesterday':
      timeParam.value = 1;
      break;
    case 'today':
      timeParam.value = 0;
      break;
    case 'analyze':
      Toast.fail("正在开发敬请期待")
      break;
    default:
      timeParam.value = 0;
  }
  fetchAnalysePictureVo();
};

const active = ref(0);
const currentRate = ref(0);
const text = "卡路里";

const caloriesRate = ref(0);
const proteinRate = ref(0);
const carbohydrateRate = ref(0);
const fatRate = ref(0);

const recentUploads = ref([]);
const showOptions = ref(false);
const showCaloriesPopup = ref(false);
const showProfilePopup = ref(false);

const imageType = ref('food');
const imageDescription = ref('');
const age = ref('');
const height = ref('');
const weight = ref('');
const fitnessGoal = ref('weekly');
const goalType = ref('lose');
const maintainGrams = ref('');

onMounted(async () => {
  await fetchUserProfile();
  await fetchAnalysePictureVo();
  //console.log("执行了这一步："+status.value)
  if(status.value==0){
    Toast.fail("用户分析等待中")
  }else if (status.value==1){
    Toast.fail("用户分析分析中");
  }else if(status.value==4){
    Toast.fail("点击右下角蓝色加号完善个人信息");
  }
});


const fetchUserProfile = async () => {
  try {
    const response = await myAxios.get('/fitness/getUserInfo');
    if (response.code === 0) {
      const userData = response.data;


      age.value = userData.age.toString();
      height.value = userData.height.toString();
      weight.value = userData.weight.toString();


      fitnessId.value = userData.fitnessId;
      caloriesValue.value = parseInt(userData.calorieTarget.toString());
      proteinValue.value = parseInt(userData.proteinTarget.toString());
      carbohydrateValue.value = parseInt(userData.carbohydrateTarget.toString());
      fatValue.value = parseInt(userData.fatTarget.toString());
      status.value = userData.status;
    }else{
      Toast.fail(response.message);
    }
  } catch (error) {
    console.error('Error fetching user profile:', error);
    Toast.fail("获取用户信息失败");
  }
};


const fitnessId = ref<number | null>(null);



const handleProfileButtonClick = () => {
  if (status.value === 0) {
    Toast.fail('正在等待');
  } else if (status.value === 1) {
    Toast.fail('正在分析');
  } else if (status.value === 2) {
    openProfilePopup();
  }else if (status.value === 4) {
    showProfilePopup.value=true;
  }
};

const openCaloriesPopup = () => {
  showOptions.value = false;
  showCaloriesPopup.value = true;
};


const openProfilePopup = async () => {
  showOptions.value = false;
  showProfilePopup.value = true;
  await fetchUserProfile();
};

const showAIAnalysisPopup = ref(false);
const proteinValue = ref(0);
const carbohydrateValue = ref(0);
const fatValue = ref(0);
const caloriesValue = ref(0);
const proteinDiff = ref(0);
const carbohydrateDiff = ref(0);
const fatDiff = ref(0);
const caloriesDiff = ref(0);

const confirmAIAnalysis = async () => {
  try {
    const response = await myAxios.get('/fitness/ai/analyseUser');
    if (response.code === 0) {
      const { calorieTarget, proteinTarget, carbohydrateTarget, fatTarget } = response.data;
      caloriesValue.value = parseInt(calorieTarget.toString());
      proteinValue.value = parseInt(proteinTarget.toString());
      carbohydrateValue.value = parseInt(carbohydrateTarget.toString());
      fatValue.value = parseInt(fatTarget.toString());
      Toast.success('AI 分析完成');
    } else {
      Toast.fail('AI 分析失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error analyzing user:', error);
    Toast.fail('网络错误，请检查您的连接');
  } finally {
    showAIAnalysisPopup.value = false;
  }
};

const cancelAIAnalysis = () => {
  showAIAnalysisPopup.value = false;
};

const submitProfile = async () => {
  if (!age.value || !height.value || !weight.value) {
    Toast.fail('请填写完整信息');
    return;
  }

  let target = '';
  if (fitnessGoal.value === 'weekly') {
    if (goalType.value === 'lose') {
      target = `每周减脂${maintainGrams.value}g`;
    } else if (goalType.value === 'gain') {
      target = `每周增肌${maintainGrams.value}g`;
    } else if (goalType.value === 'maintain') {
      target = '每周维持';
    }
  } else if (fitnessGoal.value === 'monthly') {
    if (goalType.value === 'lose') {
      target = `每月减脂${maintainGrams.value}g`;
    } else if (goalType.value === 'gain') {
      target = `每月增肌${maintainGrams.value}g`;
    } else if (goalType.value === 'maintain') {
      target = '每月维持';
    }
  } else if (fitnessGoal.value === 'bimonthly') {
    if (goalType.value === 'lose') {
      target = `每两个月减脂${maintainGrams.value}g`;
    } else if (goalType.value === 'gain') {
      target = `每两个月增肌${maintainGrams.value}g`;
    } else if (goalType.value === 'maintain') {
      target = '每两个月维持';
    }
  }

  const requestData = {
    id: fitnessId.value,
    age: parseInt(age.value),
    height: parseFloat(height.value),
    weight: parseFloat(weight.value),
    target: target,
  };

  try {
    const response = await myAxios.post('/fitness/saveOrUpdate', requestData);
    if (response.code === 0) {
      //console.log('Submitted profile:', requestData);
      showProfilePopup.value = false;
      Toast.success('信息提交成功');
      showAIAnalysisPopup.value = true;
    } else {
      Toast.fail('信息提交失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error submitting profile:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};
watch(imageType, (newType) => {
  if (newType === 'food') {
    imageDescription.value = '这是';
  } else if (newType === 'ingredient') {
    imageDescription.value = '配料表';
  }
});


const uploadedImageUrl = ref('');


const onRead = async (file) => {
  try {
    const formData = new FormData();
    formData.append('multipartFile', file.file);


    const response = await myAxios.post('/fitness/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    if (response.code === 0) {

      uploadedImageUrl.value = response.data;
      Toast.success('图片上传成功');
    } else {
      Toast.fail('图片上传失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error uploading image:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};


const foodName = ref('');


const submitImage = async () => {
  if (!imageDescription.value || !foodName.value) {
    Toast.fail('请填写完整信息');
    return;
  }

  try {

    const requestData = {
      description: imageDescription.value,
      pictureUrl: uploadedImageUrl.value,
      foodName: foodName.value,
      fitnessId: fitnessId.value,
    };


    // todo 报错原因是requestData中的fitnessId不对
    const response = await myAxios.post('/fitness/analysePicture', requestData);
    if (response.code === 0) {
      //console.log('图片分析成功:', response.data);
      showCaloriesPopup.value = false; 弹窗
      Toast.success('图片上传成功');
    } else {
      Toast.fail('图片分析失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error submitting image:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};

const fetchAnalysePictureVo = async () => {
  try {
    const response = await myAxios.get(`/fitness/image/getInfo/${timeParam.value}`);
    if (response.code === 0) {
      //console.log('获取分析图片信息成功:', response.data);
      // 根据后端返回的数据更新页面数据
      const analysePictureVoList = response.data;
      if (analysePictureVoList && analysePictureVoList.length > 0) {
        // 初始化累加变量
        let totalProtein = 0;
        let totalCarbohydrate = 0;
        let totalFat = 0;
        let totalCalories = 0;
        analysePictureVoList.forEach(item => {
          if (item.status === 1) {
            totalProtein += item.protein;
            totalCarbohydrate += item.carbohydrate;
            totalFat += item.fat;
            totalCalories += item.calorie;
          }
        });

        // 将后端返回的数据映射为 recentUploads 的格式
        recentUploads.value = analysePictureVoList.map(item => ({
          id: item.id,
          name: item.foodName,
          calories: item.calorie.toFixed(2), // 格式化为两位小数
          image: item.pictureUrl,
          time: new Date(item.createTime).toLocaleTimeString(), // 转换为时间格式
          protein: item.protein.toFixed(2),
          carbohydrate: item.carbohydrate.toFixed(2),
          fat: item.fat.toFixed(2),
          count: item.count,
          status: item.status,
          type: item.type,
          description: item.description,
          pictureUrl: item.pictureUrl,
        }));

        // 计算差值
        proteinDiff.value = proteinValue.value - totalProtein;
        carbohydrateDiff.value = carbohydrateValue.value - totalCarbohydrate;
        fatDiff.value = fatValue.value - totalFat;
        caloriesDiff.value = caloriesValue.value - totalCalories;

        // 计算百分比，确保负值时圆圈归0
        caloriesRate.value = caloriesDiff.value < 0 ? 0 : (caloriesDiff.value / caloriesValue.value) * 100;
        proteinRate.value = proteinDiff.value < 0 ? 0 : (proteinDiff.value / proteinValue.value) * 100;
        carbohydrateRate.value = carbohydrateDiff.value < 0 ? 0 : (carbohydrateDiff.value / carbohydrateValue.value) * 100;
        fatRate.value = fatDiff.value < 0 ? 0 : (fatDiff.value / fatValue.value) * 100;
      } else {
        // 当 analysePictureVoList 为空时，使用用户目标值
        proteinDiff.value = proteinValue.value;
        carbohydrateDiff.value = carbohydrateValue.value;
        fatDiff.value = fatValue.value;
        caloriesDiff.value = caloriesValue.value;

        caloriesRate.value = 100;
        proteinRate.value = 100;
        carbohydrateRate.value = 100;
        fatRate.value = 100;
        recentUploads.value = [];
      }
    } else {
      Toast.fail('获取分析图片信息失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error fetching analyse picture info:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};

const navigateToPhotoAnalyse = (item) => {
  if (item.type === 2) {
    // type=2 时跳转页面
    router.push({
      name: 'PhotoAnalyse',
      params: {
        id: item.id,
        name: item.name,
        calories: item.calories,
        image: item.image,
        time: item.time,
        protein: item.protein,
        carbohydrate: item.carbohydrate,
        fat: item.fat,
        count: item.count,
        status: item.status,
        type: item.type,
        description: item.description,
        pictureUrl: item.pictureUrl,
      },
    });
  } else if (item.type === 0) {
    // type=0 时显示提示弹窗，内容为“正在等待”
    Toast.fail('正在等待ai中');
  } else if (item.type === 1) {
    // type=1 时显示提示弹窗，内容为“正在分析”
    Toast.fail('正在分析');
  }else if (item.type === 3){
    Toast.fail('分析失败');
  }
};
</script>

<style scoped>

.button-group {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}

.button-group .van-button {
  width: 30%;
  font-size: 16px;
}

.van-row {
  margin-bottom: 16px;
}

.calories-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.calories-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.calories-value {
  font-size: 60px;
  margin-left: 40px;
}

.calories-label {
  font-size: 16px;
  margin-top: 5px;
}

.nutrient-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20px;
  text-align: center;
}

.nutrient-value {
  font-size: 18px;
  font-weight: bold;
  margin-top: 5px;
}

.nutrient-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.nutrition-section {
  display: flex;
  justify-content: space-around;
  margin-bottom: 20px;
}


.upload-popup {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-popup img {
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.upload-popup .van-uploader {
  margin-bottom: 16px;
}

.upload-popup .van-radio-group {
  margin-bottom: 16px;
}

.upload-popup .van-field {
  width: 100%;
  margin-bottom: 16px;
}


.options-popup {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}


.profile-popup {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.profile-popup .van-field {
  width: 100%;
  margin-bottom: 16px;
}


.ai-analysis-popup {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.ai-analysis-popup h3 {
  margin-bottom: 20px;
}

.ai-analysis-popup .buttons {
  width: 100%;
}


.status-indicator {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  margin-top: 8px;
}

.waiting {
  background-color: #409eff;
  color: white;
}

.analyzing {
  background-color: #f56c6c;
  color: white;
}


.status-indicator.using {
  background-color: #e6a23c;
  color: white;
  font-weight: bold;
}
</style>