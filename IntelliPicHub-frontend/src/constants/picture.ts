/**
 * 图片审核枚举类
 */

export const PIC_REVIEW_STATUS_ENUM = {
  REVIEWING: 0,
  PASS: 1,
  REJECT: 2,
}

export const PIC_REVIEW_STATUS_MAP = {
  0: 'Reviewing',
  1: 'Pass',
  2: 'Reject',
}

// Object.keys() 返回一个对象“所有可枚举属性的名称”组成的数组，这里得到["0", "1", "2"]
export const PIC_REVIEW_STATUS_OPTIONS = Object.keys(PIC_REVIEW_STATUS_MAP).map((key) => {
  return {
    label: PIC_REVIEW_STATUS_MAP[key],
    value: key,
  }
})
// 最终得到
//   [
//     { label: 'Reviewing', value: '0' },
//     { label: 'Pass', value: '1' },
//     { label: 'Reject', value: '2' }
//   ]


export const PICTURE_EDIT_MESSAGE_TYPE_ENUM = {
  INFO: 'INFO',
  ERROR: 'ERROR',
  ENTER_EDIT: 'ENTER_EDIT',
  EXIT_EDIT: 'EXIT_EDIT',
  EDIT_ACTION: 'EDIT_ACTION',
};


export const PICTURE_EDIT_MESSAGE_TYPE_MAP = {
  INFO: 'Send notification',
  ERROR: 'Send error',
  ENTER_EDIT: 'Enter edit status',
  EXIT_EDIT: 'Exit edit status',
  EDIT_ACTION: 'Execute edit action',
};

export const PICTURE_EDIT_ACTION_ENUM = {
  ZOOM_IN: 'ZOOM_IN',
  ZOOM_OUT: 'ZOOM_OUT',
  ROTATE_LEFT: 'ROTATE_LEFT',
  ROTATE_RIGHT: 'ROTATE_RIGHT',
};

export const PICTURE_EDIT_ACTION_MAP = {
  ZOOM_IN: 'Zoom in',
  ZOOM_OUT: 'Zoom out',
  ROTATE_LEFT: 'Rotate left',
  ROTATE_RIGHT: 'Rotate right',
};
