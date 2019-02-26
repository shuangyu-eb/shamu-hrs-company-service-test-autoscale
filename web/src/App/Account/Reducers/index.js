import { handleActions } from 'redux-actions';

const initialState = {
  errorVisible: false,
  errorDescription: '',
  resetPwdSuccess: false,
};

const accountReducers = handleActions({
  SET_ERROR_MSG: (state, action) => ({
    ...state,
    errorVisible: action.payload.errorVisible,
    errorDescription: action.payload.errorDescription,
  }),
  RESET_PWD_SUCCESS: (state, action) => ({
    ...state,
    resetPwdSuccess: action.payload,
  }),
}, initialState);

export default accountReducers;
