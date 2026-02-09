package com.tejpratapsingh.motionstore.infra

import android.content.Context
import com.tejpratapsingh.motionstore.dao.MotionProjectDao

fun provideMotionDbHelper(context: Context): AppDatabaseHelper = AppDatabaseHelper(context)

internal fun provideMotionProjectDao(helper: AppDatabaseHelper): MotionProjectDao = MotionProjectDao(helper.writableDatabase)
