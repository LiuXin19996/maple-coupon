export default {
  namespaced: true,
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || ''
  }),
  mutations: {
    setUser(state, payload) {
      state.user = payload.user
      state.token = payload.token
      state.refreshToken = payload.refreshToken
      localStorage.setItem('token', payload.token)
      localStorage.setItem('refreshToken', payload.refreshToken)
    },
    clearAuth(state) {
      state.user = null
      state.token = ''
      state.refreshToken = ''
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  },
  actions: {
    async login({ commit }, userInfo) {
      try {
        const response = await login(userInfo)
        commit('setUser', {
          user: response.user,
          token: response.token,
          refreshToken: response.refreshToken
        })
        return response
      } catch (error) {
        throw error
      }
    },
    async refreshToken({ commit, state }) {
      try {
        const response = await refreshToken(state.refreshToken)
        commit('setUser', {
          user: state.user,
          token: response.token,
          refreshToken: response.refreshToken
        })
        return response
      } catch (error) {
        commit('clearAuth')
        throw error
      }
    }
  }
}
