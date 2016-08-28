      SUBROUTINE CONNE(IK1,N,NB,INDEX,LABEL,STACK,ICON)
C *** *******************************************************
C     *                                                     *
C     * ROUTINE FOR CHECKING THE CONNECTIVITY OF A GRAPH    *
C     *                                                     *
C *** *******************************************************
C     *                                                     *
C     * INPUT:                                              *
C     * N  NUMBER OF NODES                                  *
C     * NB(I)  CUMULATED LIST OF NEIGHBOURS                 *
C     *        (I-1,...,2*M)                                *
C     * INDEX(I) INDEX OF THE FIRST NEIGHBOUR OF NODE I     *
C     *        IN NEIGHBOURLIST NB (I-1,...,N+1)            *       
C     * OUTPUT:                                             *
C     * ICON  - 1 IF THE GRAPH IS CONNECTTED                *
C     *       - 0 ELSE                                      *
C     *                                                     *
C     *                                                     *
C *** *******************************************************
      IMPLICIT INTEGER*4 (A-Z)
      DIMENSION NB(IK1),INDEX(IK1),LABEL(IK1),STACK(IK1)
      DO 10 I=2,N
   10 LABEL(I)=1
      LABEL(1)=0
      ICON=1
      STACK(1)=1
      KSTACK=1
   15 I=STACK(KSTACK)
      KSTACK=KSTACK-1
      I1=INDEX(I)
      I2=I+1
      I2=INDEX(I2)-1
      DO 30 J=I1,I2
      JJ=NB(J)
      IF(LABEL(JJ).EQ.O) GOTO 30
      ICON=ICON+1
      IF(ICON.EQ.N) GOTO 40
      LABEL(JJ)=0
      KSTACK=KSTACK+1
      STACK(KSTACK)=JJ
   30 CONTINUE
      IF(KSTACK.EQ.O) GOTO 40
      GOTO15
   40 ICON=ICON/N
      RETURN
      END
