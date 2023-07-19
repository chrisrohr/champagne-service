<template>
 <div class="container mx-auto px-4 h-full">
   <div class="flex content-center items-center justify-center h-full">
     <div class="w-full lg:w-4/12 px-4">
       <div class="relative flex flex-col min-w-0 break-words w-full mb-6 shadow-lg rounded-lg bg-gray-200 border-0">
         <div class="rounded-t mb-0 px-6 py-6">
           <div class="text-center mb-3">
             <h6 class="text-gray-500 text-sm font-bold">
               Sign in with
             </h6>
           </div>
           <div class="btn-wrapper text-center">
             <button class="bg-white active:bg-gray-50 text-gray-700 font-normal px-4 py-2 rounded outline-none focus:outline-none mr-2 mb-1 uppercase shadow hover:shadow-md inline-flex items-center font-bold text-xs ease-linear transition-all duration-150" type="button">
               <i class="fab fa-github w-5 mr-1"></i>
               GitHub
             </button>
             <button class="bg-white active:bg-gray-50 text-gray-700 font-normal px-4 py-2 rounded outline-none focus:outline-none mr-2 mb-1 uppercase shadow hover:shadow-md inline-flex items-center font-bold text-xs ease-linear transition-all duration-150" type="button">
               <i class="fab fa-gitlab w-5 mr-1"></i>
               GitLab
             </button>
             <button class="bg-white active:bg-gray-50 text-gray-700 font-normal px-4 py-2 rounded outline-none focus:outline-none mr-2 mb-1 uppercase shadow hover:shadow-md inline-flex items-center font-bold text-xs ease-linear transition-all duration-150" type="button">
               <i class="fab fa-google w-5 mr-1"></i>
               Google
             </button>
           </div>
           <hr class="mt-6 border-b-1 border-blueGray-300" />
         </div>
         <div class="flex-auto px-4 lg:px-10 py-10 pt-0">
           <div class="text-gray-400 text-center mb-3 font-bold">
             <small>Or sign in with credentials</small>
           </div>
           <form>
             <div class="relative w-full mb-3">
               <label class="block uppercase text-gray-600 text-xs font-bold mb-2">
                 Username
               </label>
               <input type="text" @keyup.enter="login" v-model="username" class="border-0 px-3 py-3 placeholder-gray-300 text-gray-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150" placeholder="Username"/>
             </div>

             <div class="relative w-full mb-3">
               <label class="block uppercase text-gray-600 text-xs font-bold mb-2">
                 Password
               </label>
               <input type="password" @keyup.enter="login" v-model="password" class="border-0 px-3 py-3 placeholder-gray-300 text-gray-600 bg-white rounded text-sm shadow focus:outline-none focus:ring w-full ease-linear transition-all duration-150" placeholder="Password"/>
             </div>

             <div class="text-center mt-6">
               <button
                   class="bg-gray-800 text-white active:bg-gray-600 text-sm font-bold uppercase px-6 py-3 rounded shadow hover:shadow-lg outline-none focus:outline-none mr-1 mb-1 w-full ease-linear transition-all duration-150"
                   type="button"
                   @click="login"
               >
                 Sign In
               </button>
             </div>

             <div class="text-center mt-6" v-if="showLoginError">
               <div class="text-white px-6 py-4 border-0 rounded relative mb-4 bg-amber-500">
                 <small>Unable to login with current credentials!</small>
               </div>
             </div>
           </form>
         </div>
       </div>
     </div>
   </div>
 </div>
</template>

<script setup>
import {ref} from "vue";
import {useCurrentUserStore} from "@/stores/currentUser";

const currentUserStore = useCurrentUserStore();

const username = ref('');
const password = ref('');
const showLoginError = ref(false);

function login() {
  if (username.value === '' || password.value === '') {
    showLoginError.value = true;
  } else {
    currentUserStore.login(username.value, password.value);
  }
}
</script>
