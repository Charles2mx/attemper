import { login, getInfo } from '@/api/sys/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router, { resetRouter } from '@/router'
import { closeAllTabs } from '@/utils/tools'

const state = {
  token: getToken(),
  name: '',
  displayName: '',
  avatar: '',
  introduction: '',
  roles: [],
  resourceNames: []
}
const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_INTRODUCTION: (state, introduction) => {
    state.introduction = introduction
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_DISPLAY_NAME: (state, displayName) => {
    state.displayName = displayName
  },
  SET_RESOURCE_NAMES: (state, resourceNames) => {
    state.resourceNames = resourceNames
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  }
}
const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login(username.trim(), password).then(response => {
        const result = response.data.result
        commit('SET_NAME', result.user.userName)
        commit('SET_DISPLAY_NAME', result.user.displayName)
        commit('SET_TOKEN', result.token)
        setToken(result.token)
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo().then(response => {
        const { data } = response
        if (!data) {
          reject('Verification failed, please Login again.')
        }
        const { result } = data
        commit('SET_NAME', result.user.userName)
        commit('SET_DISPLAY_NAME', result.user.displayName)
        const roleNames = []
        if (result.tags && result.tags.length) {
          result.tags.forEach(tag => {
            if (tag.tagType === 0) {
              roleNames.push(tag.tagName)
            }
          })
          commit('SET_ROLES', roleNames)
          sessionStorage.roleNames = JSON.stringify(roleNames)
        }
        const resourceNames = []
        if (result.resources && result.resources.length) {
          result.resources.forEach(resource => {
            resourceNames.push(resource.resourceName)
          })
          commit('SET_RESOURCE_NAMES', resourceNames)
          sessionStorage.resourceNames = JSON.stringify(resourceNames)
        }
        resolve(resourceNames)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      /* logout(state.token).then(() => {
        commit('SET_TOKEN', '')
        commit('SET_ROLES', [])
        removeToken()
        resetRouter()
        resolve()
      }).catch(error => {
        reject(error)
      })*/
      commit('SET_TOKEN', '')
      commit('SET_ROLES', [])
      sessionStorage.removeItem('roleNames')
      commit('SET_RESOURCE_NAMES', [])
      sessionStorage.removeItem('resourceNames')
      removeToken()
      closeAllTabs()
      resolve()
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      commit('SET_TOKEN', '')
      commit('SET_ROLES', [])
      removeToken()
      resolve()
    })
  },

  // Dynamically modify permissions
  changeRoles({ commit, dispatch }, role) {
    return new Promise(async resolve => {
      const token = role + '-token'

      commit('SET_TOKEN', token)
      setToken(token)

      const { roles } = await dispatch('getInfo')

      resetRouter()

      // generate accessible routes map based on roles
      const accessRoutes = await dispatch('permission/generateRoutes', roles, { root: true })

      // dynamically add accessible routes
      router.addRoutes(accessRoutes)

      resolve()
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}