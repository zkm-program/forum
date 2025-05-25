<template>
  <div class="breakfast-info">
    <div class="food-image">
      <img :src="pictureUrl" alt="食物图片" />
      <div class="food-name">{{ foodName }}</div>
    </div>
    <van-cell title="营养成分">
      <template #default>
        <div class="nutrition-info">
          <van-row gutter="20">
            <van-col span="12">
              <div class="nutrition-item">
                <van-icon name="fire-o" /> 卡路里 {{ calculatedCalorie }}
              </div>
              <div class="nutrition-item">
                <van-icon name="records" /> 蛋白质 {{ calculatedProtein }} g
              </div>
            </van-col>
            <van-col span="12">
              <div class="nutrition-item">
                <van-icon name="flower-o" /> 碳水化合物 {{ calculatedCarbohydrate }} 克
              </div>
              <div class="nutrition-item">
                <van-icon name="balance-pay" /> {{ calculatedFat }} 克 脂肪
              </div>
            </van-col>
          </van-row>
        </div>
      </template>
    </van-cell>

    <van-cell title="描述">
      <template #default>
        <div class="description">{{ description }}</div>
      </template>
    </van-cell>
    <van-cell title="数量">
      <template #default>
        <van-stepper v-model="quantity" />
      </template>
    </van-cell>
    <div class="bottom-buttons">
      <van-button v-if="status === 0" type="default" disabled>你没有吃它</van-button>
      <van-button v-else-if="status === 1" type="default" disabled>你已经把它吃了</van-button>
      <div v-else-if="status === 3" class="button-container">
        <van-button type="default" @click="noEat">不吃了</van-button>
        <van-button type="primary" @click="eat">吃掉它</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import { ref, computed, onMounted } from 'vue';
import { Toast } from 'vant';
import myAxios from '../plugins/myAxios';

const router = useRouter();
const route = useRoute();
const id = ref(route.params.id || '');
const pictureUrl = ref(route.params.pictureUrl || '');
const foodName = ref(route.params.name || '');
const calorie = ref(route.params.calories || '');
const protein = ref(route.params.protein || '');
const carbohydrate = ref(route.params.carbohydrate || '');
const fat = ref(route.params.fat || '');
const description = ref(route.params.description || '');
const quantity = ref(1);
const type = ref(Number(route.params.type || 0));
const status = ref(Number(route.params.status || 0));
const calculatedCalorie = computed(() => (parseFloat(calorie.value) * quantity.value).toFixed(2));
const calculatedProtein = computed(() => (parseFloat(protein.value) * quantity.value).toFixed(2));
const calculatedCarbohydrate = computed(() => (parseFloat(carbohydrate.value) * quantity.value).toFixed(2));
const calculatedFat = computed(() => (parseFloat(fat.value) * quantity.value).toFixed(2));
const goBack = () => {
  router.back();
};

const noEat = async () => {
  try {
    const requestData = {
      flag: 0,
      id: id.value,
    };

    const response = await myAxios.post('/fitness/image/selectIfEat', requestData);

    if (response.code === 0) {
      Toast.success('操作成功');
      router.back();
    } else {
      Toast.fail('操作失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error calling selectIfEat:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};

const eat = async () => {
  try {
    const requestData = {
      flag: 1,
      id: id.value,
    };

    const response = await myAxios.post('/fitness/image/selectIfEat', requestData);

    if (response.code === 0) {
      Toast.success('操作成功');
      router.back();
    } else {
      Toast.fail('操作失败，请稍后重试');
    }
  } catch (error) {
    console.error('Error calling selectIfEat:', error);
    Toast.fail('网络错误，请检查您的连接');
  }
};

// const fetchAnalysePictureVo = async () => {
//   try {
//     const response = await myAxios.get('/getInfo');
//     if (response.code === 0) {
//       console.log('获取分析图片信息成功:', response.data);
//       const analysePictureVoList = response.data;
//       if (analysePictureVoList && analysePictureVoList.length > 0) {
//         const firstItem = analysePictureVoList[0];
//         pictureUrl.value = firstItem.pictureUrl || '';
//         foodName.value = firstItem.foodName || '';
//         calorie.value = firstItem.calorie || '';
//         protein.value = firstItem.protein || '';
//         carbohydrate.value = firstItem.carbohydrate || '';
//         fat.value = firstItem.fat || '';
//       }
//     } else {
//       Toast.fail('获取分析图片信息失败，请稍后重试');
//     }
//   } catch (error) {
//     console.error('Error fetching analyse picture info:', error);
//     Toast.fail('网络错误，请检查您的连接');
//   }
// };

// onMounted(() => {
   // fetchAnalysePictureVo();
// });
</script>

<style scoped>
.breakfast-info {
  padding: 20px;
}

.food-image {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.food-image img {
  width: 100%;
  border-radius: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.food-name {
  position: absolute;
  top: 50%;
  left: 20px;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

.calorie {
  position: absolute;
  top: 50%;
  right: 20px;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.9);
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

.nutrition-info {
  margin: 15px 0;
}

.nutrition-item {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.nutrition-item .van-icon {
  margin-right: 8px;
  font-size: 20px;
}


.van-cell {
  padding: 12px;
}


.bottom-buttons {
  display: flex;
  justify-content: center;
  gap: 25px;
  margin-top: 30px;
}

.bottom-buttons .van-button {
  padding: 10px 25px;
  font-size: 16px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.button-container {
  display: flex;
  gap: 25px;
}
</style>